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
  [es doc]
  (s/request es {:url
                 [(cfg/es-index) :_doc (str (:id doc))]
                 :headers {"Content-Type" "application/json"}
                 :method :put
                 :body doc}))

;; (defn entity-indexed?
;;   [es entity]
;;   (s/request es {:})
;;   )

;; ;; PUT https://<host>:<port>/<index-name>/_doc/<document-id>
;; {
;;   "title": "The Wind Rises",
;;   "release_date": "2013-07-20"
;; }
