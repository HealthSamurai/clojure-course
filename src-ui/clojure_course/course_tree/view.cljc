(ns clojure-course.course-tree.view
  (:require [clojure-course.pages]
            [clojure-course.course-tree.model :refer [course-tree-s
                                                      select-module
                                                      select-chapter]]
            [zf]
            [stylo.core :refer [c]]))


(def classes
  {:marker                       (c :grid [:rounded 100] [:w 9] {:place-items  "center"
                                                                 :aspect-ratio 1})
   :marker--none                 (c {:background-color "var(--grey-neutral-color)"})
   :marker--success              (c {:background-color "var(--success-color)"})
   :marker--error                (c {:background-color "var(--error-color)"})
   :marker--alert                (c {:background-color "var(--alert-color)"})})


(zf/defv course-tree [course-tree-s]
  [:div {:class (c :flex :flex-col :justify-between :h-full)}
   [:style "details>summary {

  list-style: none;
}
summary::-webkit-details-marker {
  display: none
}

summary::before {
  margin-right: 10px;
  margin-bottom: 2px;
  width: 1ex;
  height: 1ex;
  display: inline-block;
  content: \" \";
  background-position: center;
  background-image: url(\"/static/images/nav-arrow-down.svg\");
}
details[open] summary:before {
  margin-right: 10px;
  margin-bottom: 2px;
  width: 1ex;
  height: 1ex;
  display: inline-block;
  background-position: center;
  content: \" \";
  background-image: url(\"/static/images/nav-arrow-right.svg\");
}"]
   [:div {:class (c :overflow-y-scroll [:mb 15])}
    (for [[module-title module] (:modules course-tree-s)]
      [:details {:key module-title
                 :class (c [:mb 10])
                 :on-click #(select-module module-title)}
       [:summary {:class (c :font-bold {:font-size "2.4rem"} :capitalize
                            :transition-opacity {:transition-duration "70ms"} :ease-in
                            :cursor-pointer [:hover [:opacity 60] ])}
        module-title]
       [:div
        [:div {:class (c  [:mt 5] [:mb 8] [:h 3] [:rounded 4] {:background-color "#E3E3E3"})}
         [:div {:class (c :h-full [:rounded 4] {:background-color "var(--success-color)"})
                :style {:width (str (get-in module [:stats :module-progress]) "%")}}]]
        [:ol {:class (c :grid [:row-gap 12])}
         (for [[chapter-title chapter] (:chapters module)]
           [:li {:key chapter-title
                 :class (c :grid [:row-gap 8])}
            [:div {:class (c :flex :justify-between :items-center)
                   :on-click #(select-chapter chapter-title)}
             [:p {:class (c {:font-size "1.8rem"} :capitalize
                            :transition-opacity {:transition-duration "70ms"} :ease-in
                            :cursor-pointer [:hover [:opacity 60]])}
              chapter-title]
             (if (> (:passed (:stats chapter)) 0)
               [:div {:class [(:marker classes) (:marker--success classes)]}
                [:img {:src "/static/images/check-mark.svg"
                       :alt "success icon"}]]
               [:div {:class [(:marker classes) (:marker--none classes)]}])]
            [:ol {:class (c :grid [:row-gap 8])}
             (for [[test-title test-content] (:tests chapter)]
               [:li {:key test-title}
                [:div {:class (c :flex :justify-between :items-center)}
                 [:p {:class (c [:ml 5])} test-title]
                 (case (:status test-content)
                   "new"
                   [:div {:class [(:marker classes) (:marker--none classes)]}]

                   "error"
                   [:div {:class [(:marker classes) (:marker--none classes)]}]

                   "failed"
                   [:div {:class [(:marker classes) (:marker--none classes)]}]

                   "passed"
                   [:div {:class [(:marker classes) (:marker--success classes)]}
                    [:img {:src "/static/images/check-mark.svg"
                           :alt "success icon"}]]

                   [:div {:class [(:marker classes) (:marker--none classes)]}])]])]])]]])]

   (let [stats (:stats course-tree-s)]
     [:div {:class (c :flex [:col-gap 20] [:rounded 16] [:py 8] [:px 15]
                      {:background-color "var(--main-background-color)"})}
      [:div {:class (c :flex :flex-col :items-center [:row-gap 3])}
       [:p {:class (c :flex {:font-size "1.2rem"})}
        "Received"]
       [:p {:class (c :font-bold {:font-size "4.2rem"})}
        (:total stats)]]
      [:div {:class (c :flex :flex-col :items-center [:row-gap 3])}
       [:p {:class (c :flex {:font-size "1.2rem"})}
        [:img {:src "/static/images/check-circle.svg"
               :alt "success icon"
               :class (c [:mr 2])}]
        [:span "Done"]]
       [:p {:class (c :font-bold {:font-size "4.2rem"})}
        (or (:passed stats) 0)]]
      [:div {:class (c :flex :flex-col :items-center [:row-gap 3])}
       [:p {:class (c :flex {:font-size "1.2rem"})}
        "Errors"]
       [:p {:class (c :font-bold {:font-size "4.2rem"})}
        (+ (:error stats) (:failed stats))]]])])


(clojure-course.pages/reg-page clojure-course.course-tree.model/page course-tree)
