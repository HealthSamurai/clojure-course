(ns course-content.frontend
  (:require [stylo.rule]))


(defn c* [& args]
  (stylo.rule/join-rules args))


(def styles
  [[:.course-content :h1 (c* :border-b {:font-size "32px" :margin-top "10px" :margin-bottom "16px" :font-weight "600"})]
   [:.course-content :h2 (c* :border-b {:font-size "24px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "30px"})]
   [:.course-content :h3 (c* :border-b {:font-size "20px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "25px"})]
   [:.course-content :h4 (c* {:font-size "16px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "20px"})]
   [:.course-content :h5 (c* {:font-size "14px" :margin-top "20px" :margin-bottom "14px" :font-weight "600" :line-height "16px"})]
   [:.course-content :ul (c* [:ml 4] [:mb 4])
    {:list-style "inside"
     :line-height "24px"}
    [:li {:display "list-item"
          :list-style "outside"}]
    [:ul (c* [:mb 0])]]
   [:.course-content :ol (c* [:ml 4]
            {:list-style "disk inside"
             :line-height "24px"})
    [:li (c* [:my 1]
             {:display "list-item"
              :list-style "outside decimal"})]
    [:ol (c* [:ml 4])]]

   [:.course-content :p (c* [:mb 4] {:line-height "1.5rem"})]

   [:.course-content :.hljs (c* [:bg :gray-100] :shadow-sm
               :border)]
   [:.course-content :.active-nav {:border-bottom "2px solid #666" :color "black"}]
   [:.course-content :pre {:margin-top "1rem" :margin-bottom "1rem"}]
   [:.course-content :.closed {:display "none"}]
   [:.course-content :.bolder (c* :font-bold)]
   [:.course-content :.nlSaver {:white-space "pre-wrap"}]
   [:.course-content :.searchResultContainer (c* [:px 6] [:py 3] :flex :flex-col
                                [:hover :cursor-pointer [:bg :gray-200]])]
   [:.course-content :.searchResultContainerRow (c* :flex)]
   [:.course-content :.searchResultContainerSummaryRow (c* :border-t)]
   [:.course-content :.searchResultContainerVBar (c* [:h "30px"] [:w "2px"]
                                    :rounded [:mr 2] [:bg :blue-500])]
   [:.course-content :.badge
    [:p {:margin-bottom -3}]]

   [:.course-content :.visible {:visibility "visible"}]
   [:.course-content :.pl-4  {:padding-left "1rem"}]
   [:.course-content :.toggler-arrow {:padding-left "4px"
               :padding-right "4px"
               :padding-top "2px"
               :padding-bottom "2px"}]
   [:.course-content :.rotateToggler {:transform "rotate(-90deg)"}]
   [:.course-content :.searchContainer {:position "fixed"
                       :width "90%"
                       :height "100%"
                       :top 0
                       :transition "transform 0.3s 0.3s"}]


   [:.course-content :.mindmap
    [:.node
     [:circle {:fill "#aaa"}]
     [:text {}]]
    [:.node--internal [:circle {:fill "#999"}]]
    [:.link {:fill "none"
             :stroke "#aaa"
             :stroke-opacity "0.4"
             :stroke-width "1.5px"}]]

   [:.course-content :.zd-toggle [:.zd-content {:height "0"
                               :transform "scaleY(0) "
                               :transform-origin "top"
                               :transition "all 0.26s ease"}]]
   [:.course-content :.zd-toggle.zd-open
    [:.zd-content {:transform "scaleY(1)"
                   :height "auto"}]
    [:.zd-block-title [:.fas {:transform "rotate(90deg)"
                              :transition "all 0.26s"}]]]])
