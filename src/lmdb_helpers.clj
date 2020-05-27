(ns lmdb-helpers
  (:import (java.nio ByteBuffer)
           (java.nio.charset StandardCharsets)))

(defn build-env!
  "Build env from an EnvBuilder instance"
  [env-builder map-size max-dbs dir-file open-flags]
  (-> env-builder
      (.setMapSize map-size)
      (.setMaxDbs max-dbs)
      (.open dir-file
             open-flags)))

(defn read-val [buf]
  (let [byte-a (byte-array (.limit buf))]
    (.get buf byte-a)
    (clojure.edn/read-string
     (String. byte-a StandardCharsets/UTF_8))))

(defn put!
  [db env txn key val]
  (let [key* (ByteBuffer/allocateDirect (.getMaxKeySize env))
        val-str (str val)
        val* (ByteBuffer/allocateDirect (* 4 (count val-str)))]
    (.flip (.put key* (.getBytes (str key) StandardCharsets/UTF_8)))
    (.flip (.put val* (.getBytes val-str)))
    (.put db txn key* val* nil)))

(defn get!
  [db env txn key]
  (let [key* (ByteBuffer/allocateDirect (.getMaxKeySize env))]
    (.flip (.put key*
                 (.getBytes (str key)
                            StandardCharsets/UTF_8)))
    (when-let [buf (.get db txn key*)]
      (read-val buf))))

(defn iter>seq [it]
  []
  (when (.hasNext it)
    (let [p (.next it)]
      (lazy-seq (cons [(read-val (.key p))
                       (read-val (.val p))]
                      (iter>seq it))))))
