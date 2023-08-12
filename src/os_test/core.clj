(ns os-test.core
  (:require [qbits.spandex :as s]))

(defn init-es
  "Establishes a connection to elasticsearch"
  []
  (let [url      "https://localhost:9200"
        host-map {:hosts [url]}
        opts     (merge host-map {:http-client {:basic-auth {:user "admin" :password "admin"}}})
        conn     (s/client opts)]
    (if conn
      (do
        (println (format "Successfully connected to Elasticsearch: %s" url))
        conn)
      (do
        (println "Failed to find elasticsearch. Retrying...")
        (Thread/sleep 1000)
        (recur)))))
