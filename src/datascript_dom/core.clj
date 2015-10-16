(ns datascript-dom.core
  (:require [datascript.core :as d]
            [pl.danieljanus.tagsoup :as html]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [clojure.string :as string]))

(def schema
  {:parent       {:db/valueType :db.type/ref}
   :prev-sibling {:db/valueType :db.type/ref}})

(def rules
  '[[(root ?node)
     [?node _ _]
     [(missing? $ ?node :parent)]]

    ;;ancestor
    [(anc ?par ?child)
     (?child :parent ?par)]
    [(anc ?anc ?child)
     (?child :parent ?par)
     (anc ?anc ?par)]

    ;;sibling
    [(siblings ?a ?b)
     [?a :parent ?p]
     [?b :parent ?p]
     [(!= ?a ?b)]]

    [(next-sibling ?this ?next)
     [?next :prev-sibling ?this]]
    
    [(next-sibling ?this ?prev)
     [?this :prev-sibling ?prev]]

    ;;get all text of text nodes in container
    [(text ?container ?text)
     [?text-node :parent ?container]
     [?text-node :tag :text-node]
     [?text-node :text ?text]]

    [(text ?container ?text)
     [?container :tag :text-node]
     [?container :text ?text]]

    [(path2 ?a ?b)
     [?b :parent ?a]]
    [(path3 ?a ?b ?c)
     (path2 ?a ?b)
     (path2 ?b ?c)]])

(defn parse-html-file [filename]
  (html/parse (io/file filename)))

(defn as-node [node]
  (if-not (string? node)
    node
    [:text-node {:text node}]))

(defn- node-to-transaction [node parent prev]
  (merge
   {:tag (first node)}
   (html/attributes node)
   (when-let [id (:id node)] {:dom-id id})
   (when parent {:parent parent})
   (when prev {:prev-sibling prev})))

(defn has-children? [node]
  (and (vector? node) (> (count node) 2)))

(defn set-children [node children]
  (concat (take 2 node) children))

(defn replace-node [zipper fun]
  (zip/replace zipper (fun (zip/node zipper))))

(defn dom->transaction [dom]
  (let [walk (fn walk [zipper id]
               (when-not (zip/end? zipper)
                 (let [zipper    (replace-node zipper as-node)
                       parent-id (some-> zipper zip/up zip/node html/attributes :db/id)
                       left      (some-> zipper zip/left zip/node)
                       left-id   (some-> left html/attributes :db/id)
                       zipper    (replace-node
                                  zipper
                                  #(-> %
                                       (assoc-in [1 :db/id] id)
                                       (assoc-in [1 :dom/index]
                                                 (if (nil? left)
                                                   0 (inc (some-> left html/attributes :dom/index))))))]
                   (cons (node-to-transaction (zip/node zipper) parent-id left-id)
                         (lazy-seq (walk (zip/next zipper) (dec id)))))))]
    (walk (zip/zipper has-children? html/children set-children dom) -1)))

(defn- dump [dom]
  (let [z (zip/zipper has-children? html/children set-children dom)]
    (loop [z z]
      (prn (zip/node z))
      (if-not (zip/end? z) (recur (zip/next z))))))

(defn get-touch [db id]
  (d/touch (d/entity db id)))

(comment

  ;;small toy DOM
  (def small-dom (html/parse-string "<html><body><p>hehe<b>he</b></p><p>hohoho</p></body></html>"))
  ;; [:html {} [:body {} [:p {} "hehe" [:b {} "he"]] [:p {} "hohoho"]]]
  (def small-conn (d/create-conn schema))
  (d/transact small-conn (dom->transaction small-dom))

  ;;way more serious IMDB DOM
  (def dom (parse-html-file "resources/tron.html"))
  (def conn (d/create-conn schema))
  (def _ (d/transact cc (dom->transaction dom)))


  ;; THE FOLLOWING QUERIES ARE BEST APPLIED TO THE SMALL DOM (see parse-string above)
  
  ;;find all the tags that have a previous sibling
  (def r
   (map
    (partial get-touch @conn)
    (d/q '[:find [?node ...]
           :where
           [?node :prev-sibling _]] @small-conn)))

  ;;find all the tags that have a next sibling
  (def r
   (map
    (partial get-touch @conn)
    (d/q '[:find [?node ...]
           :where
           [_ :prev-sibling ?node]] @small-conn)))

  ;;find root - returns the actual map because of the pull API
  (def r
    (d/q '[:find (pull ?node [*]) .
           :where
           [?node _ _] ;;not sure why we need this
           [(missing? $ ?node :parent)]] @small-conn))

  ;;get the body tag by doing an inverse pull on the root
  (def r
    (d/q '[:find (pull ?node [{:_parent [*]}])
           :where
           [?node _ _] ;;not sure why we need this
           [(missing? $ ?node :parent)]] @small-conn))

  ;;get the children of the children of root with all their attributes
  (def r
    (d/q '[:find (pull ?node [{:_parent [{:_parent [*]}]}])
           :where
           [?node _ _] ;;not sure why we need this
           [(missing? $ ?node :parent)]] @small-conn))

  ;;find root with a rule - returns the id because of the simple find
  (def r
    (d/q '[:find ?node .
           :in $ %
           :where (root ?node)]
         @small-conn
         '[[(root ?node)
            [?node _ _]
            [(missing? $ ?node :parent)]]]))

  (->> r (d/entity @conn) :_parent first ;;gets <body> tag
       :_parent (map d/touch)) ;;gets the two paragraph tag
  
  ;;get all ancestors of <b>
  (def r
    (d/q '[:find [(pull ?anc [*]) ...]
           :in $ %
           :where
           [?node :tag :b]
           (anc ?anc ?node)]
         @small-conn
         '[[(anc ?par ?child)
            (?child :parent ?par)]
           [(anc ?anc ?child)
            (?child :parent ?par)
            (anc ?anc ?par)]])) ;;recursive rule yo!

  (def r
    (d/q '[:find [(pull ?sib [*]) ...]
           :in $ %
           :where
           [?node :tag :p]
           (siblings ?node ?sib)]
         @small-conn rules))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;;                                                         ;;
  ;;   THE FOLLOWING QUERIES ARE DESIGNED FOR THE IMDB DOM   ;;
  ;;                                                         ;;
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  
  ;;failed attempt to find the title
  (def r
    (d/q '[:find ?text
           :where
           [?node :tag :span]
           [?node :itemprop "name"]
           [?text-node :parent ?node]
           [?text-node :text ?text]]
         @conn))

  ;;find title
  (def r
    (d/q '[:find ?text .
           :where
           [?h1 :tag :h1]
           [?node :parent ?h1]
           [?node :tag :span]
           [?node :itemprop "name"]
           [?text-node :parent ?node]
           [?text-node :text ?text]]
         @conn))

  ;;find title and year with text extractor rule
  (let [[title year]
        (d/q '[:find [?title ?year]
               :in $ %
               :where
               [?h1 :tag :h1]
               [?name-node :parent ?h1]
               [?name-node :tag :span]
               [?name-node :itemprop "name"]
               (text ?name-node ?title)

               (next-sibling ?name-node ?year-node) ;;year-node is the next sibling of name-node
               (?a-node :parent ?year-node) ;;...and it contains an <a> tag
               (?a-node :tag :a)
               (text ?a-node ?year)] ;;and the <a> tag contains the year
             @conn rules)]
    {:title title
     :year  year})

  ;;overview
  (d/q '[:find ?overview .
         :in $ %
         :where
         [?node :itemprop "description"]
         (text ?node ?overview)]
       @conn rules)
  
  ;;ratings
  (d/q '[:find [?rating ?count]
         :in $ %
         :where
         [?star-node :class "titlePageSprite star-box-giga-star"]
         (text ?star-node ?rating)

         [?rating-count :itemprop "ratingCount"]
         (text ?rating-count ?count)]
       @conn rules)

  ;;reviews
  (d/q '[:find ?reviews
         :in $ %
         :where
         [?review-node :itemprop "reviewCount"]
         (text ?review-node ?reviews)]
       @conn rules)

  ;;Extract Cast - part 1
  ;;The important guys follow this pattern (actors whose names have
  ;;links). Note that you get one entry per character, the same actor
  ;;can depict more than one character.
  (clojure.pprint/pprint
   (let [cast
         (d/q '[:find ?index ?actor-name ?link ?character-name ?character-link
                :in $ %
                :where

                ;;extract the index of the row so that we know the order
                [?actor-n :parent ?tr]
                [?tr :dom/index ?index]

                ;;extract the name of the actor and the link to their profile
                [?actor-n :itemprop "actor"]
                (path3 ?actor-n ?actor-link-n ?actor-name-n)
                (text ?actor-name-n ?actor-name)
                (?actor-link-n :href ?link)

                ;;extract the name of the character(s) played by the actor and link(s)
                (siblings ?character-n ?actor-n)
                [?character-n :class "character"]
                (path3 ?character-n ?div ?character-link-n)
                (?character-link-n :href ?character-link)
                (text ?character-link-n ?character-name)]
              @conn rules)]
     (->> (map (partial zipmap [:index :actor :link :character :character-link]) cast)
          (sort-by :index))))

  ;;Extract Cast - part 2
  ;;the unimportant guys follow this pattern
  ;;(actors whose names don't have links)
  (def r
    (sort-by first
     (d/q '[:find ?index ?actor-name ?link ?character-name
            :in $ % ?trim
            :where
            [?actor-n :parent ?tr]
            [?tr :dom/index ?index]
           
            [?actor-n :itemprop "actor"]
            (path3 ?actor-n ?actor-link-n ?actor-name-n)
            (text ?actor-name-n ?actor-name)
            (?actor-link-n :href ?link)

            (siblings ?character-n ?actor-n)
            [?character-n :class "character"]
            (path2 ?character-n ?div)

            ;;the above matches the important characters as well, so we
            ;;clean the name and if we matched the "/" between the
            ;;character links, we filter those entries out
            (text ?div ?character-name-dirty)
            [(?trim ?character-name-dirty) ?character-name]
            [(!= "/" ?character-name)]]
          @conn rules (fn [x] (.trim (string/replace x #"\s+" " "))))))

  ;;Extract genres
  (d/q '[:find [?g ...]
         :in $ %
         :where
         [?div :itemprop "genre"]
         [?a :parent ?div]
         [?a :tag :a]
         (text ?a ?g)]
       @conn rules)

  ;;Duration
  (d/q '[:find ?duration .
         :in $ %
         :where
         [?node :itemprop "duration"]
         (text ?node ?duration)]
       @conn rules)

  (defn tech-spec [conn label]
    (->>
     (d/q '[:find ?index ?value-text
            :in $ % ?label-text
            :where
            [?label :tag :h4]
            [?label :class "inline"]
            (text ?label ?label-text)

            (siblings ?label ?value)
            (text ?value ?value-text)
            [(!= "|" ?value-text)]
            (?value :dom/index ?index)]
          @conn rules label)
     (sort-by first)
     (map (comp (fn [x] (.trim x)) second))))

  (tech-spec conn "Runtime:")
  (tech-spec conn "Color:")
  (tech-spec conn "Sound Mix:")
  (tech-spec conn "Aspect Ratio:")

  ;;also works on other labelled fields!
  (tech-spec conn "Budget:")
  (tech-spec conn "Gross:")
  (tech-spec conn "Language:")
  
  )

