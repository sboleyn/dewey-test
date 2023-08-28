(ns os-test.indexing
  (:require [qbits.spandex :as s]
            [os-test.config :as cfg]
            [os-test.entity :as entity]
            [clojure-commons.file-utils :as file]
            [os-test.doc-prep :as prep]))

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


;; client
;; {:keys [method url headers query-string body keywordize?
;;         response-consumer-factory exception-handler]
;;  :or {method :get
;;       keywordize? true
;;       exception-handler default-exception-handler
;;       response-consumer-factory
;;       HttpAsyncResponseConsumerFactory/DEFAULT}
;;  :as request-params})


;; POST /test-index1/_update/1
;; {
;;   "script" : {
;;     "source": "ctx._source.secret_identity = \"Batman\""
;;   }
;; }

;; {
;;   "script" : {
;;     "source": "ctx._source.oldValue += params.newValue",
;;     "lang": "painless",
;;     "params" : {
;;       "newValue" : 10
;;     }
;;   }
;; }
(defn- update-doc
  "Scripted updates which are only compatible with Elasticsearch 5.x and greater."
  [c entity script params]
  (s/request c {:url [(cfg/es-index) :_update (str (entity/id entity))]
                :method :post
                :headers {"Content-Type" "application/json"}
                ;;:body {:script {:inline script :lang "painless" :params params}}}))
                :body {"script" {"source" script "lang" "painless" "params" params}}}))

(defn index-doc
  "I tested this one and it indexes."
  [c doc]
  (s/request c {:url
                [(cfg/es-index) :_doc (str (:id doc))]
                :method :put
                :headers {"Content-Type" "application/json"}
                :body doc}))

(defn entity-indexed?
  ;; DOC FIX
  ;; Fix ability to pass in id
  ;; (ns were-creatures)
  ;; ➊ (defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))
  ;; ➋ (defmethod full-moon-behavior :wolf
  ;;     [were-creature]
  ;;     (str (:name were-creature) " will howl and murder"))
  ;; ➌ (defmethod full-moon-behavior :simmons
  ;;     [were-creature]
  ;;     (str (:name were-creature) " will encourage people and sweat to the oldies"))


  ;; (full-moon-behavior {:were-type :wolf
  ;; ➍                      :name "Rachel from next door"})
     ; => "Rachel from next door will howl and murder"
  [c entity]
  ^{:doc "Determines whether or not an iRODS entity has been indexed.

             Parameters:
               c     - the elasticsearch connection
               entity - the entity being checked

             Throws:
               This function can throw an exception if FIX"}
  (try
    (s/request c {:url [(cfg/es-index) :_doc (str (entity/id entity))]
    ;;(s/request c {:url [(cfg/es-index) :_doc "0f14883e-37fa-11ec-8a85-28924acd781foo"]
                  :method :head}) true
    (catch Exception e (println (format "Error %s" e)) false)))

; XXX - I wish I could think of a way to cleanly and simply separate out the document update logic
; from the update scripts calls in the following functions. It really belongs with the rest of the
; document logic in the doc-prep namespace.

(defn update-path
  "Updates the path of an entity and optionally its modification time

   Parameters:
     c       - the elasticsearch connection
     entity   - the entity to update
     path     - the entity's new path
     mod-time - (Optional) the entity's modification time"
  ([c entity path]
   (update-doc c
               entity
               "ctx._source.path = params.path;
                ctx._source.label = params.label;"
               {:path  path
                :label (file/basename path)}))

  ([c entity path mod-time]
   (update-doc c
               entity
               "ctx._source.path = params.path;
                 ctx._source.label = params.label;
                 if (params.dateModified > ctx._source.dateModified) { ctx._source.dateModified = params.dateModified }"
               {:path         path
                :label        (file/basename path)
                :dateModified (prep/format-time mod-time)})))

(defn update-acl
  "Updates the indexed ACL of an entity.

   Parameters:
     c     - the elasticsearch connection
     entity - the entity whose ACL needs to be updated in elasticsearch

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. It can also
     throw if the entity has no index entry or is not in the iRODS data store."
  [c entity]
  (update-doc c
              entity
              "ctx._source.userPermissions = params.permissions"
              {:permissions (prep/format-acl (entity/acl entity))}))
