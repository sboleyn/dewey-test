(ns os-test.indexing
  (:require [qbits.spandex :as s]
            [os-test.config :as cfg]
            [os-test.entity :as entity]))

(def docu {:id "0209c39e-37ef-11ec-8a85-28924acd7818"
           :doc_type "file"
           :path "/cyverse/home/tswetnam/ept/ept-data/8-77-7-132.laz"
           :label "8-77-7-132.laz"
           :creator "de-irods#cyverse"
           :dateCreated 1656707727000
           :dateModified 1656707727000
           :fileSize 205090
           :fileType "generic"
           :userPermissions [{:user "de-irods#cyverse"
                              :permission "own"}
                             {:user "rodsadmin#cyverse"
                              :permission "own"}]
           :metadata {:irods [{:attribute "ipc-filetype"
                               :value "unknown"

                               :unit "ipc-info-typer"}]}})

(defn index-doc
  [c doc]
  (s/request c {:url
                [(cfg/es-index) :_doc (str (:id doc))]
                :method :put
                :headers {"Content-Type" "application/json"}
                :body doc}))

(defn entity-indexed?
  [c entity]
  (try
    (s/request c {:url [(cfg/es-index) :_doc "0f14883e-37fa-11ec-8a85-28924acd7818"]
    ;;(s/request c {:url [(cfg/es-index) :_doc "0f14883e-37fa-11ec-8a85-28924acd781foo"]
                  :method :head}) true
    (catch Exception e (println (format "Error %s" e)) false)))

;; client
;; {:keys [method url headers query-string body keywordize?
;;         response-consumer-factory exception-handler]
;;  :or {method :get
;;       keywordize? true
;;       exception-handler default-exception-handler
;;       response-consumer-factory
;;       HttpAsyncResponseConsumerFactory/DEFAULT}
;;  :as request-params})
