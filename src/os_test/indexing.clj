(ns os-test.indexing
  "This is the logic for making changes to search index."
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

(defn- index-doc
  [c doc]
  (s/request c {:url
                [(cfg/es-index) :_doc (str (:id doc))]
                :method :put
                :headers {"Content-Type" "application/json"}
                :body doc}))

(defn- update-doc
  "Scripted updates which are only compatible with Elasticsearch 5.x and greater."
  [c entity script params]
  (s/request c {:url [(cfg/es-index) :_update (str (entity/id entity))]
                :method :post
                :headers {"Content-Type" "application/json"}
                ;;:body {:script {:inline script :lang "painless" :params params}}}))
                :body {"script" {"source" script "lang" "painless" "params" params}}}))

;; (defn entity-indexed?
;;   ;; DOC FIX
;;   ;; Fix ability to pass in id
;;   ;; (ns were-creatures)
;;   ;; ➊ (defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))
;;   ;; ➋ (defmethod full-moon-behavior :wolf
;;   ;;     [were-creature]
;;   ;;     (str (:name were-creature) " will howl and murder"))
;;   ;; ➌ (defmethod full-moon-behavior :simmons
;;   ;;     [were-creature]
;;   ;;     (str (:name were-creature) " will encourage people and sweat to the oldies"))


;;   ;; (full-moon-behavior {:were-type :wolf
;;   ;; ➍                      :name "Rachel from next door"})
;;      ; => "Rachel from next door will howl and murder"
;;   [c entity]
;;   ^{:doc "Determines whether or not an iRODS entity has been indexed.

;;              Parameters:
;;                c     - the elasticsearch connection
;;                entity - the entity being checked

;;              Throws:
;;                This function can throw an exception if FIX"}
;;   (try
;;     (s/request c {:url [(cfg/es-index) :_doc (str (entity/id entity))]
;;     ;;(s/request c {:url [(cfg/es-index) :_doc "0f14883e-37fa-11ec-8a85-28924acd781foo"]
;;                   :method :head}) true
;;     (catch Exception e (println (format "Error %s" e)) false)))

(defn indexing-method
  [entity]
  (cond
    (string? entity) :string
    (map? entity) :map
    :else :unknown))

(defmulti entity-indexed? (fn [c entity] (indexing-method entity)))
(defmethod entity-indexed? :string
  [c entity-id]
  (try
    (s/request c {:url [(cfg/es-index) :_doc entity-id]
                  :method :head}) true
    (catch Exception e (println (format "Error %s" e)) false)))
(defmethod entity-indexed? :map
  [c entity]
  (try
    (s/request c {:url [(cfg/es-index) :_doc (str (entity/id entity))]
                  :method :head}) true
    (catch Exception e (println (format "Error %s" e)) false)))


(defn index-collection
  "Indexes a collection.

   Parameters:
     c    - the elasticsearch connection
     coll  - the collection entity to index

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. The
     function can also throw one if the collection is not in the iRODS data store."
  [c coll]
  (let [folder (prep/format-folder (entity/id coll)
                                   (entity/path coll)
                                   (entity/acl coll)
                                   (entity/creator coll)
                                   (entity/creation-time coll)
                                   (entity/modification-time coll)
                                   (entity/metadata coll))]
    (index-doc c folder)))

(defn index-data-object
  "Indexes a data object.

   Parameters:
     c        - the elasticsearch connection
     obj       - The CollectionAndDataObjectListingEntry of the data object to index
     creator   - (Optional) The username of the creator of the data object
     file-size - (Optional) The byte size of the data object
     file-type - (Optional) The media type of the data object

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. The
     function can also throw one if the data object is not in the iRODS data store."
  [c obj & {:keys [creator file-size file-type]}]
  (let [file (prep/format-file (entity/id obj)
                               (entity/path obj)
                               (entity/acl obj)
                               (or creator (entity/creator obj))
                               (entity/creation-time obj)
                               (entity/modification-time obj)
                               (entity/metadata obj)
                               (or file-size (entity/size obj))
                               (or file-type (entity/media-type obj)))]
    (index-doc c file)))


;; (s/request es {:url ["data_test1" :_doc "0f0f3be6-377c-11ec-8a85-28924acd7818"] :method :delete})

(defn remove-entity
  "Removes an iRODS entity from the search index.

   Parameters:
     c          - the elasticsearch connection
     entity-type - :collection|:data-object
     entity-id   - The UUID of the entity to remove

   Throws:
     This function can throw an exception if it can't connect to elasticsearch."
  [c entity-id]
  (when (entity-indexed? c (str entity-id))
    (s/request c {:url [(cfg/es-index) :_doc (str entity-id)]})))

(defn remove-entities-like
  "Removes iRODS entities from the search index that have a path matching the provide glob. The glob
   supports * and ? wildcards with their typical meanings.

   This method uses the Elasticsearch Delete By Query API, and is not backward compatible with
  Elasticsearch versions prior to 5.x.

   Parameters:
     c        - the elasticsearch connection
     path-glob - the glob describing the paths of the entities to remove

   Throws:
     This function can throw an exception if it can't connect to elasticsearch."

  ;; {:keys [method url headers query-string body keywordize?
;;         response-consumer-factory exception-handler]
;;  :or {method :get
;;       keywordize? true
;;       exception-handler default-exception-handler
;;       response-consumer-factory
;;       HttpAsyncResponseConsumerFactory/DEFAULT}
;;  :as request-params})

  [c path-glob]
  ;; (rest/post es
  ;;            (rest/url-with-path es (cfg/es-index) "_delete_by_query")
  ;;            {:body {:query (es-query/wildcard :path path-glob)}}))

  ;; POST <index>/_delete_by_query

  (s/request c {:url [(cfg/es-index) :_delete_by_query]
                :query-string "analyze_wildcard=true"
                :method :post
                :body {:query {:wildcard {:path path-glob}}}}))

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

(defn update-metadata
  "Updates the indexed AVU metadata of an entity.

   Parameters:
     c     - the elasticsearch connection
     entity - The entity whose metadata needs to be updated in elasticsearch

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. It can also
     throw if the entity has no index entry or is not in the iRODS data store."
  [c entity]
  (update-doc c
              entity
              "ctx._source.metadata = params.metadata"
              {:metadata (prep/format-metadata (entity/metadata entity))}))

(defn update-collection-modify-time
  "Updates the indexed modify time of a collection.

   Parameters:
     c   - the elasticsearch connection
     coll - the collection that was modified

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. It can
     also throw if the collection has no index entry or is not in the iRODS data store."
  [c coll]
  (update-doc c
              coll
              "if (params.dateModified > ctx._source.dateModified) { ctx._source.dateModified = params.dateModified } else { ctx.op = \"none\" }"
              {:dateModified (prep/format-time (entity/modification-time coll))}))

(defn update-data-object
  "Updates the indexed data object. It will update the modification time, file size and optionally
   file type for the data object.

   Parameters:
     c        - the elasticsearch connection
     obj       - the data object that was modified
     file-size - The data object's file size in bytes.
     file-type - (OPTIONAL) The media type of the data object.

   Throws:
     This function can throw an exception if it can't connect to elasticsearch or iRODS. It can
     also throw if the data object has no index entry or is not in the iRODS data store."
  ([c obj file-size]
   (update-doc c
               obj
               "if (params.dateModified > ctx._source.dateModified) { ctx._source.dateModified = params.dateModified; }
                ctx._source.fileSize = params.fileSize;"
               {:dateModified (prep/format-time (entity/modification-time obj))
                :fileSize     file-size}))

  ([c obj file-size file-type]
   (update-doc c
               obj
               "if (params.dateModified > ctx._source.dateModified) { ctx._source.dateModified = params.dateModified; }
                ctx._source.fileSize = params.fileSize;
                ctx._source.fileType = params.fileType;"
               {:dateModified (prep/format-time (entity/modification-time obj))
                :fileSize     file-size
                :fileType     file-type})))
