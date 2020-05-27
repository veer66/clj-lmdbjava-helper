# clj-lmdbjava-helper

## Sample

### src/sample.clj

````Clojure
(ns sample
  (:require [lmdb-helpers :as h])
  (:import (org.lmdbjava Env EnvFlags DbiFlags KeyRange)))

(defn -main
  [& args]
  (let [dir-path "."
        env (h/build-env! (Env/create)
                          100000
                          1
                          (clojure.java.io/as-file dir-path)
                          nil)        
        db (.openDbi env "db1" (into-array DbiFlags
                                           [DbiFlags/MDB_CREATE]))
        txn (.txnWrite env)]
    (h/put! db env txn "a" 1)
    (h/put! db env txn "b" 2)
    (h/put! db env txn "c" {:m "x"})
    (.commit txn)
    (let [txn (.txnRead env)
          it (.iterate db txn (KeyRange/all))]
      (prn (first (h/iter>seq it)))
      (prn (h/get! db env txn "c"))
      (.close txn)
      (.close db)
      (.close env))))
````
