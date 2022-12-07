(ns clojure-course.content
  (:require [clojure.string :as str]
            [garden.core]
            [garden.selectors]
            [stylo.rule]))


(defn c* [& args]
  (stylo.rule/join-rules args))


(def content-root-class "course-content-root")


(def base-styles
  [#_[:* garden.selectors/before garden.selectors/after {:overflow-x "auto"}]
   [:h1 (c* :border-b {:font-size "32px" :margin-top "10px" :margin-bottom "16px" :font-weight "600"})]
   [:h2 (c* :border-b {:font-size "24px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "30px"})]
   [:h3 (c* :border-b {:font-size "20px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "25px"})]
   [:h4 (c* {:font-size "16px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "20px"})]
   [:h5 (c* {:font-size "14px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "16px"})]
   [:ul (c* [:ml 4] [:mb 4])
    {:list-style "inside"
     :line-height "24px"}
    [:li {:display "list-item"
          :list-style "outside"}]
    [:ul (c* [:mb 0])]]
   [:ol (c* [:ml 4]
            {:list-style "disk inside"
             :line-height "24px"})
    [:li (c* [:my 1]
             {:display "list-item"
              :list-style "outside decimal"})]
    [:ol (c* [:ml 4])]]
   [:p (c* [:mb 4] {:line-height "1.5rem"})]
   [:.hljs (c* [:bg :gray-100] :shadow-sm
               :border)]
   [:.active-nav {:border-bottom "2px solid #666" :color "black"}]
   [:pre {:margin-top "1rem" :margin-bottom "1rem"}]
   [:.closed {:display "none"}]
   [:.bolder (c* :font-bold)]
   [:.nlSaver {:white-space "pre-wrap"}]
   [:.searchResultContainer (c* [:px 6] [:py 3] :flex :flex-col
                                [:hover :cursor-pointer [:bg :gray-200]])]
   [:.searchResultContainerRow (c* :flex)]
   [:.searchResultContainerSummaryRow (c* :border-t)]
   [:.searchResultContainerVBar (c* [:h "30px"] [:w "2px"]
                                    :rounded [:mr 2] [:bg :blue-500])]
   [:.badge
    [:p {:margin-bottom -3}]]

   [:.visible {:visibility "visible"}]
   [:.pl-4  {:padding-left "1rem"}]
   [:.toggler-arrow {:padding-left "4px"
               :padding-right "4px"
               :padding-top "2px"
               :padding-bottom "2px"}]
   [:.rotateToggler {:transform "rotate(-90deg)"}]
   [:.searchContainer {:position "fixed"
                       :width "90%"
                       :height "100%"
                       :top 0
                       :transition "transform 0.3s 0.3s"}]
   [:.mindmap
    [:.node
     [:circle {:fill "#aaa"}]
     [:text {}]]
    [:.node--internal [:circle {:fill "#999"}]]
    [:.link {:fill "none"
             :stroke "#aaa"
             :stroke-opacity "0.4"
             :stroke-width "1.5px"}]]
   [:.zd-toggle [:.zd-content {:height "0"
                               :transform "scaleY(0) "
                               :transform-origin "top"
                               :transition "all 0.26s ease"}]]
   [:.zd-toggle.zd-open
    [:.zd-content {:transform "scaleY(1)"
                   :height "auto"}]
    [:.zd-block-title [:.fas {:transform "rotate(90deg)"
                              :transition "all 0.26s"}]]]])


(def base-styles-css (garden.core/css clojure-course.content/base-styles))


(def font-awesome-css-url "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css")


(def highlightjs-css-url "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.3.1/styles/default.min.css")


(def highlightjs-js-main-url "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.3.1/highlight.min.js")


(def highlightjs-js-clojure-url "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.3.1/languages/clojure.min.js")
