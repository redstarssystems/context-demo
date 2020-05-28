(ns my.example.ctxdemo.web.pages.index
  (:require [unifier.response :as r]
            [my.example.ctxdemo.web.pages.templates :as templates]))

(defn handler
  "index '/' page handler"
  [req]
  (r/as-success
    (templates/main
      [:div.jumbotron.text-center
       [:h2 "index page"]])))

