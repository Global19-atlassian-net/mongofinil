(ns mongofinil.test-hooks
  (:require [bond.james :as bond]
            [somnium.congomongo :as congo]
            [mongofinil.core :as core]
            [mongofinil.testing-utils :as utils])
  (:use midje.sweet
        [clojure.core.incubator :only (-?>)])
  (:import org.bson.types.ObjectId))


(utils/setup-test-db)
(utils/setup-midje)

(defn update-hook [row]
  (update-in row [:hook-count] (fnil inc 0)))

(defn load-hook [row]
  (update-in row [:loaded] (fnil inc 0)))

(core/defmodel :xs
  :fields [{:name :x :findable true}]
  :hooks {:update {:post #'update-hook}
          :load {:post #'load-hook}})

(fact "hooks are triggered"
  (bond/with-spy [load-hook]
    (let [orig (create! {})]
      orig => (contains {:hook-count 1})
      (-> load-hook bond/calls count) => 1)))

(fact "hooks aren't called when no rows are returned"
  (bond/with-spy [load-hook]
    (seq (find-one :where {:bogus 987})) => nil
    (-> load-hook bond/calls count) => 0))
