(ns my.example.ctxdemo.web.middleware.security
  (:require [unifier.response :as r]
            [io.pedestal.log :as log]
            [my.example.ctxdemo.web.pages.templates :as templates]
            [clojure.string :as str])
  (:import (inet.ipaddr IPAddressString)))

(defn- admin-network?
  "Check if remote address belongs to admin networks (coll of Strings)."
  [valid-nets-list ^String remote-addr]
  (let [addr (IPAddressString. remote-addr)]
    (if (some #(.contains (IPAddressString. %) addr) (remove str/blank? valid-nets-list))
      true
      false)))

(defn admin-network-check
  "Middleware checks if request belongs to admin networks."
  [handler admin-networks]
  (fn [request]
    (if (admin-network? admin-networks (:remote-addr request))
      (do
        (log/info :msg "request from authorized network" :remote-addr (:remote-addr request) :uri (:uri request))
        (handler request))
      (do
        (log/error :msg "request from unauthorized network"
          :remote-addr (:remote-addr request)
          :uri (:uri request)
          :admin-networks admin-networks)
        (r/as-forbidden
          (templates/simple [:a {:href "/"}
                             [:image {:src "/img/admins-sign.png" :width 200 :height 200}]]))))))
