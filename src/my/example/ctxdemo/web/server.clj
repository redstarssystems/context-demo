(ns my.example.ctxdemo.web.server
  (:require [ring.adapter.jetty9 :as jetty]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [io.pedestal.log :as log]
            [my.example.ctxdemo.web.pages.templates :as templates]
            [my.example.ctxdemo.web.router :as router]
            [my.example.ctxdemo.web.middleware.response :as response])
  (:import (org.eclipse.jetty.server Server)))


(defn app
  [*ctx]
  (ring/ring-handler
    (router/routes *ctx)
    (ring/routes
      (ring/create-resource-handler {:path "/" :root "public"})
      (ring/create-default-handler {:not-found templates/not-found-handler}))
    {:middleware                                            ;; put your global middlewares here
     [response/unified-resp->http]}))


(defn- disable-server-fingerprint
  "disable Jetty fingerprint for security reasons"
  [^Server s]
  (doseq [cf (map #(.getConnectionFactories %) (.getConnectors s))]
    (when (instance? org.eclipse.jetty.server.HttpConnectionFactory (first cf))
      (.setSendServerVersion (.getHttpConfiguration (first cf)) false))))

(defn run
  "Run web server.
  Returns:
   * ^Server object."
  ^Server
  [handler conf]
  (log/info :m "Starting web server..." :host (:http-host conf) :port (:http-port conf))
  (let [s (jetty/run-jetty handler {:host (:http-host conf) :port (:http-port conf) :join? false})]
    (when (:http-disable-fingerprint conf) (disable-server-fingerprint s))
    s))

(defn stop
  "Stop web server.
  Returns: nil"
  [^Server server-obj]
  (log/info :m "Stopping web server...")
  (.stop server-obj))



