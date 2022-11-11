(ns clojure-course.course-tree.view
  (:require [clojure-course.pages]
            [clojure-course.course-tree.model :refer [course-tree-s]]
            [zf]
            [stylo.core :refer [c]]))


(def classes
  {:course-progress              (c :flex :flex-col :justify-between :h-full)
   :module-title                 (c :font-bold {:font-size "2.4rem"})
   :chapter-title                (c {:font-size "1.8rem"})
   :progress-bar-full            (c [:h 3] [:rounded 4] {:background-color "#E3E3E3"})
   :progress-bar-filled          (c :h-full [:rounded 4] {:background-color "var(--success-color)"})
   :list-chapters                (c :grid [:row-gap 16])
   :list-tests                   (c :grid [:row-gap 8])
   :progress-marker-container    (c :flex :justify-between :items-center)
   :marker                       (c :grid [:rounded 100] [:w 9] {:place-items  "center"
                                                                 :aspect-ratio 1})
   :marker--none                 (c {:background-color "var(--grey-neutral-color)"})
   :marker--success              (c {:background-color "var(--success-color)"})
   :marker--error                (c {:background-color "var(--error-color)"})
   :marker--alert                (c {:background-color "var(--alert-color)"})
   :tests-summary                (c :flex [:col-gap 25] [:my 0] :mx-auto [:rounded 6] {:width            "max-content"
                                                                                       #_#_:background-color "var(--main-background-color)"})
   :tests-summary__metric        (c :flex :flex-col :items-center [:row-gap 3])
   :tests-summary__metric-title  (c :flex {:font-size "1.2rem"})
   :tests-summary__metric-result (c :font-bold {:font-size "4.2rem"})
   })


(zf/defv course-tree [course-tree-s]
  [:div {:class [(:course-progress classes)]}

   [:div
    (for [[module-title module] (:modules course-tree-s)]
      [:div {:key module-title}
       [:h2 {:class [(:module-title classes)]}
        module-title]
       [:div {:class [(:progress-bar-full classes)]}
        [:div {:class [(:progress-bar-filled classes)]
               :style {:width "75%"}}]]

       [:ol {:class [(:list-chapters classes)]}
        (for [[chapter-title chapter] (:chapters module)]
          [:li {:key chapter-title
                :class [(:list-tests classes)]}
           [:div {:class [(:progress-marker-container classes)]}
            [:p {:classes [(:chapter-title classes)]}
             chapter-title]
            (if (> (:passed (:stats chapter)) 0)
              [:div {:class [(:marker classes) (:marker--success classes)]}
               [:img {:src "/static/images/check-mark.svg"
                      :alt "success icon"}]]
              [:div {:class [(:marker classes) (:marker--none classes)]}])]
           [:ol {:class [(:list-tests classes)]}
            (for [[test-title test-content] (:tests chapter)]
              [:li {:key test-title}
               [:div {:class [(:progress-marker-container classes)]}
                [:p test-title]
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

                      [:div {:class [(:marker classes) (:marker--none classes)]}])]])]])]])]

   (let [stats (:stats course-tree-s)]
     [:div {:class [(:tests-summary classes)]}
      [:div {:class [(:tests-summary__metric classes)]}
       [:p {:class [(:tests-summary__metric-title classes)]}
        "Received"]
       [:p {:class [(:tests-summary__metric-result classes)]}
        (:total stats)]]
      [:div {:class [(:tests-summary__metric classes)]}
       [:p {:class [(:tests-summary__metric-title classes)]}
        #_[:img {:src "/static/images/check-circle.svg"
               :alt "success icon"}]
        [:span "Done"]]
       [:p {:class [(:tests-summary__metric-result classes)]}
        (or (:passed stats) 0)]]
      [:div {:class [(:tests-summary__metric classes)]}
       [:p {:class [(:tests-summary__metric-title classes)]}
        "Errors"]
       [:p {:class [(:tests-summary__metric-result classes)]}
        (+ (:error stats) (:failed stats))]]])])


(clojure-course.pages/reg-page clojure-course.course-tree.model/page course-tree)
