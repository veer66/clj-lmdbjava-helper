# clj-lmdbjava-helper

## Sample


### deps.edn

````edn
{:deps {veer66/clj-lmdbjava-helper
        {:git/url "https://github.com/veer66/clj-lmdbjava-helper"
         :sha "42af88b9812f3bc4ab460a900ccad8075f408686"}
        org.lmdbjava/lmdbjava {:mvn/version "0.7.0"}}}
````

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
