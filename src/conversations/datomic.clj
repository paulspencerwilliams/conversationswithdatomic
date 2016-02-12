(ns conversations.datomic
  (require [datomic.api :as d]
           [datomic.query :as q]))

(def uri "datomic:mem://first-conversation")

(d/create-database uri)
(def conn (d/connect uri))(d/create-database uri)
(def conn (d/connect uri))

(def dog-schema  [{:db/id (d/tempid :db.part/db)
                   :db/ident :dog/name
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique :db.unique/identity
                   :db/doc "Name of the Dog"
                   :db.install/_attribute :db.part/db}
                  {:db/id (d/tempid :db.part/db)
                   :db/ident :dog/breed
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/doc "Breed of the Dog"
                   :db.install/_attribute :db.part/db}
                  {:db/id (d/tempid :db.part/db)
                   :db/ident :dog/favorite-treat
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/doc "Dog's Favorite Treat to Eat"
                   :db.install/_attribute :db.part/db}])

(d/transact conn dog-schema)

(def owner-schema [{:db/id (d/tempid :db.part/db)
                    :db/ident :owner/name
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/unique :db.unique/identity
                    :db/doc "Name of the Owner"
                    :db.install/_attribute :db.part/db}
                   {:db/id (d/tempid :db.part/db)
                    :db/ident :owner/dogs
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many
                    :db/doc "Dogs of the Owner"
                    :db.install/_attribute :db.part/db}])

(d/transact conn owner-schema)

(d/transact conn [{:db/id (d/tempid :db.part/user)
                   :owner/name "Bob"
                   :owner/dogs [{:db/id (d/tempid :db.part/user)
                                 :dog/name "Fluffy"
                                 :dog/breed "Poodle"
                                 :dog/favorite-treat "Cheese"}
                                {:db/id (d/tempid :db.part/user)
                                 :dog/name "Fido"
                                 :dog/breed "Mix"
                                 :dog/favorite-treat "Bone"}]}
                  {:db/id (d/tempid :db.part/user)
                   :owner/name "Lucy"
                   :owner/dogs [{:db/id (d/tempid :db.part/user)
                                 :dog/name "Tiny"
                                 :dog/breed "Great Dane"
                                 :dog/favorite-treat "Cheese"}]}])


(d/q '[:find ?owner-name
       :where [?dog :dog/name "Tiny"]
       [?owner :owner/dogs ?dog]
       [?owner :owner/name ?owner-name]] (d/db conn))

(d/q '[:find ?owner-name
       :in $ ?dog-name
       :where [?dog :dog/name ?dog-name]
       [?owner :owner/dogs ?dog]
       [?owner :owner/name ?owner-name]]
     (d/db conn) "Tiny")

(d/q '[:find [(pull ?dog [:dog/name :dog/breed]) ...]
       :where [?dog :dog/favorite-treat "Cheese"]]
     (d/db conn))


(d/pull (d/db conn) '[*] [:dog/name "Tiny"])


(d/transact conn [[:db/retract [:dog/name "Tiny"] :dog/favorite-treat "Cheese"]])

(def db-tiny-no-cheese (d/db conn))

(d/pull db-tiny-no-cheese '[*] [:dog/name "Tiny"])

(d/q '[:find ?e ?a ?v ?tx ?op
       :in $
       :where [?e :dog/name "Tiny"]
       [?e ?a ?v ?tx ?op]]
     (d/history db-tiny-no-cheese))

(d/pull (d/as-of db-tiny-no-cheese 13194139534314) '[*] [:dog/name "Tiny"])
(d/pull (d/as-of db-tiny-no-cheese 13194139534323) '[*] [:dog/name "Tiny"])

(d/pull db-tiny-no-cheese '[*] [:dog/name "Fido"])

(d/transact conn [{:db/id [:dog/name "Fido"]
                   :dog/favorite-treat "Eggs"}])

(d/pull (d/db conn) '[*] [:dog/name "Fido"])