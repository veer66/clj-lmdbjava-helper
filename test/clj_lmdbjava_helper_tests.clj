(ns clj-lmdbjava-helper-tests
  (:require [lmdb-helpers :as h]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest testing run-tests is]])
  (:import (org.lmdbjava Env EnvFlags DbiFlags KeyRange)))

(defn mkdir [dir-path]
  (.mkdir (io/as-file dir-path)))

(defn del-all [dir-path]
  (when (.exists (io/as-file dir-path))
    (when (.isDirectory (io/as-file dir-path))
      (doseq [f (.listFiles (io/as-file dir-path))]
        (.delete f))))
  (.delete (io/as-file dir-path)))

(defmacro run-in-tmp
  [tmp-path body]
  `(do (del-all ~tmp-path)
       (mkdir ~tmp-path)
     (let [run-result# ~body]     
       (del-all ~tmp-path)
      run-result#)))

(deftest read-write
  (testing "basic"
    [(is
      (let [dir-path "read-write-basic-tmp"]
        (run-in-tmp dir-path              
                    (let [env (h/build-env! (Env/create)
                                            100000
                                            1
                                            (io/as-file dir-path)
                                            nil)

                          db (.openDbi env "db1" (into-array DbiFlags
                                                             [DbiFlags/MDB_CREATE]))
                          txn (.txnWrite env)]
                      (h/put! db env txn "k1" {:x 10})
                      (let [cmp-result (= {:x 10} (h/get! db env txn "k1"))]
                        (.close txn)
                        (.close db)
                        (.close env)
                        cmp-result)))))]))

(run-tests)
