(ns datascript-dom.core
  (:require [datascript.core :as d]
            [pl.danieljanus.tagsoup :as html]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [clojure.string :as string]))

(def schema
  {:child {:db/valueType   :db.type/ref
           :db/cardinality :db.cardinality/many}})

(def rules
  '[[(root ?node)
     [?node _ _]
     [(missing? $ ?node :_child)]]

    ;;ancestor
    [(anc ?par ?child)
     (?par :child ?child)]
    [(anc ?anc ?child)
     (?par :child ?child)
     (anc ?anc ?par)]

    ;;sibling
    [(siblings ?a ?b)
     [?p :child ?a]
     [?p :child ?b]
     [(!= ?a ?b)]]

    [(prev-sibling ?this ?sib)
     (siblings ?sib ?this)
     [?this :dom/index ?i1]
     [?sib :dom/index ?i2]
     [(- ?i2 ?i1) ?diff]
     [(= ?diff -1)]]
    
    [(next-sibling ?this ?sib)
     (siblings ?sib ?this)
     [?this :dom/index ?i1]
     [?sib :dom/index ?i2]
     [(- ?i2 ?i1) ?diff]
     [(= ?diff 1)]]

    ;;get all text of text nodes in container
    [(text ?container ?text)
     [?container :child ?text-node]
     [?text-node :dom/tag :text-node]
     [?text-node :text ?text]]

    [(text ?container ?text)
     [?container :dom/tag :text-node]
     [?container :text ?text]]

    [(path2 ?a ?b)
     [?a :child ?b]]
    [(path3 ?a ?b ?c)
     (path2 ?a ?b)
     (path2 ?b ?c)]])

(defn parse-html-file [filename]
  (html/parse (io/file filename)))

(defn as-node [node]
  (if-not (string? node)
    (merge
     {:dom/tag (html/tag node)}
     (dissoc (html/attributes node) :id)
     (when-let [id (:id node)] {:dom/id id})
     (when-let [children (html/children node)]
       {:child children}))
    {:dom/tag :text-node
     :text    node}))

(defn has-children? [node]
  (not-empty (:child node)))

(defn set-children [node children]
  (assoc node :child children))

(defn replace-node [zipper fun]
  (zip/replace zipper (fun (zip/node zipper))))

(defn dom->transaction [dom]
  (loop [zipper (zip/zipper has-children? :child set-children dom)]
    (if (zip/end? zipper)
      [(zip/root zipper)]
      (let [left   (some-> zipper zip/left zip/node)
            zipper (replace-node
                    zipper
                    #(assoc (as-node %) :dom/index
                            (if (nil? left)
                              ;;no-one on your left, you're the first sibling
                              0
                              ;;someone on your left, your index is their index +1
                              (some-> left :dom/index inc))))]
        (recur (zip/next zipper))))))

(defn- dump [dom]
  (let [z (zip/zipper has-children? html/children set-children dom)]
    (loop [z z]
      (prn (zip/node z))
      (if-not (zip/end? z) (recur (zip/next z))))))

(defn get-touch [db id]
  (d/touch (d/entity db id)))

(comment

  ;;small toy DOM
  (def small-dom
    (html/parse-string "<html><body class=\"test\"><p>A<b>B</b>C<i>D</i>E</p><p>hohoho</p></body></html>"))
  ;; [:html {} [:body {} [:p {} "hehe" [:b {} "he"]] [:p {} "hohoho"]]]
  (def small-conn (d/create-conn schema))
  (d/transact small-conn (dom->transaction small-dom))

  ;;way more serious IMDB DOM
  (def dom (html/parse (io/file "resources/tron.html")))
  (def conn (d/create-conn schema))
  (def _ (d/transact conn (dom->transaction dom)))


  ;; THE FOLLOWING QUERIES ARE BEST APPLIED TO THE SMALL DOM (see parse-string above)

  ;;get the <body> tag via its attribute
  (d/q '[:find (pull ?node [:dom/tag]) .
         :where
         [?node :class "test"]]
       @small-conn)
  
  ;;find root - returns the actual map because of the pull API
  (d/q '[:find (pull ?node [:dom/tag]) .
         :where
         [?node _]
         [(missing? $ ?node :_child)]]
       @small-conn)

  ;;find root with a rule - returns the id because of the simple find
  (def r
    (d/q '[:find ?node .
           :in $ %
           :where (root ?node)]
         @small-conn rules))

  (->> r (d/entity @small-conn) :child first ;;gets <body> tag
       :child (map d/touch)) ;;gets the two paragraph tags
  
  ;;get all ancestors of <b>
  (d/q '[:find [(pull ?anc [:dom/tag]) ...]
         :in $ %
         :where
         [?node :dom/tag :b]
         (anc ?anc ?node)]
       @small-conn rules)

  ;;get all siblings of <b>
  (d/q '[:find (pull ?sib [*])
         :in $ %
         :where
         [?node :dom/tag :b]
         (siblings ?node ?sib)]
       @small-conn rules)

  ;;get previous sibling of <b>
  (d/q '[:find (pull ?sib [:dom/tag :text]) .
         :in $ %
         :where
         [?node :dom/tag :b]
         (prev-sibling ?node ?sib)]
       @small-conn rules)

  ;;get next sibling of <b>
  (d/q '[:find (pull ?sib [:dom/tag :text]) .
         :in $ %
         :where
         [?node :dom/tag :b]
         (next-sibling ?node ?sib)]
       @small-conn rules)

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;;                                                         ;;
  ;;   THE FOLLOWING QUERIES ARE DESIGNED FOR THE IMDB DOM   ;;
  ;;                                                         ;;
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  
  ;;find title
  (def r
    (d/q '[:find ?text .
           :where
           [?h1 :dom/tag :h1]
           [?h1 :child ?node]
           [?node :dom/tag :span]
           [?node :itemprop "name"]
           [?node :child ?text-node]
           [?text-node :text ?text]]
         @conn))

  ;;find title and year with text extractor rule
  (let [[title year]
        (d/q '[:find [?title ?year]
               :in $ %
               :where
               [?h1 :dom/tag :h1]
               [?h1 :child ?name-node]
               [?name-node :dom/tag :span]
               [?name-node :itemprop "name"]
               (text ?name-node ?title)

               (next-sibling ?name-node ?year-node) ;;year-node is the next sibling of name-node
               (?year-node :child ?a-node) ;;...and it contains an <a> tag
               (?a-node :dom/tag :a)
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
                [?tr :child ?actor-n]
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
            [?tr :child ?actor-n]
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
         [?div :child ?a]
         [?a :dom/tag :a]
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
            [?label :dom/tag :h4]
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

