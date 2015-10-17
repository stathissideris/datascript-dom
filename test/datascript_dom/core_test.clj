(ns datascript-dom.core-test
  (:require [clojure.test :refer :all]
            [datascript-dom.core :refer :all]
            [datascript.core :as d]
            [pl.danieljanus.tagsoup :as html]
            [clojure.zip :as zip]))

(deftest test-dom->transaction
  (is (=
       [{:dom/tag :html,
         :child
         [{:dom/tag :body,
           :class "test",
           :child
           [{:dom/tag :p,
             :child
             [{:dom/tag :text-node, :text "A", :dom/index 0}
              {:dom/tag :b,
               :child [{:dom/tag :text-node, :text "B", :dom/index 0}],
               :dom/index 1}
              {:dom/tag :text-node, :text "C", :dom/index 2}
              {:dom/tag :i,
               :child [{:dom/tag :text-node, :text "D", :dom/index 0}],
               :dom/index 3}
              {:dom/tag :text-node, :text "E", :dom/index 4}],
             :dom/index 0}
            {:dom/tag :p,
             :child [{:dom/tag :text-node, :text "hohoho", :dom/index 0}],
             :dom/index 1}],
           :dom/index 0}],
         :dom/index 0}]
       (dom->transaction
        (html/parse-string "<html><body class=\"test\"><p>A<b>B</b>C<i>D</i>E</p><p>hohoho</p></body></html>")))))

(deftest basic-queries
  (let [html "<html><body class=\"test\"><p>A<b>B</b>C<i>D</i>E</p><p>hohoho</p></body></html>"
        dom  (html/parse-string html)
        conn (d/create-conn schema)]
    @(d/transact conn (dom->transaction dom))

    (is (= {:dom/tag :body}
           (d/q '[:find (pull ?node [:dom/tag]) .
                  :where
                  [?node :class "test"]] @conn)))

    (is (= {:dom/tag :html}
           (d/q '[:find (pull ?node [:dom/tag]) .
                  :where
                  [?node _]
                  [(missing? $ ?node :_child)]] @conn)))

    (is (= {:dom/tag :html}
           (d/q '[:find (pull ?node [:dom/tag]) .
                  :in $ %
                  :where
                  (root ?node)]
                @conn rules)))

    (is (= [{:dom/tag :p} {:dom/tag :body} {:dom/tag :html}]
           (d/q '[:find [(pull ?anc [:dom/tag]) ...]
                  :in $ %
                  :where
                  [?node :dom/tag :b]
                  (anc ?anc ?node)]
                @conn rules)))

    (is (= [{:dom/tag :text-node, :text "A"}]
           (d/q '[:find [(pull ?sib [:dom/tag :text]) ...]
                  :in $ %
                  :where
                  [?node :dom/tag :b]
                  (prev-sibling ?node ?sib)]
                @conn rules)))

    (is (= [{:dom/tag :text-node, :text "C"}]
           (d/q '[:find [(pull ?sib [:dom/tag :text]) ...]
                  :in $ %
                  :where
                  [?node :dom/tag :b]
                  (next-sibling ?node ?sib)]
                @conn rules)))))
