(ns datascript-dom.core-test
  (:require [clojure.test :refer :all]
            [datascript-dom.core :refer :all]
            [pl.danieljanus.tagsoup :as html]))

(deftest test-dom->transaction
  (is (=
       [{:tag :html, :db/id -1}
        {:tag :body, :parent -1, :db/id -2}
        {:tag :p, :parent -2, :db/id -3}
        {:tag :text-node, :text "A", :parent -3, :db/id -4}
        {:tag :b, :parent -3, :prev-sibling -4, :db/id -5}
        {:tag :text-node, :text "B", :parent -5, :db/id -6}
        {:tag :text-node, :text "C", :parent -3, :prev-sibling -5, :db/id -7}
        {:tag :i, :parent -3, :prev-sibling -7, :db/id -8}
        {:tag :text-node, :text "D", :parent -8, :db/id -9}
        {:tag :text-node, :text "E", :parent -3, :prev-sibling -8, :db/id -10}
        {:tag :p, :parent -2, :prev-sibling -3, :db/id -11}
        {:tag :text-node, :text "hohoho", :parent -11, :db/id -12}]
       (dom->transaction
        (html/parse-string "<html><body><p>A<b>B</b>C<i>D</i>E</p><p>hohoho</p></body></html>")))))
