;;Functional

(defn make-op [op]
  (fn [& args]
    (fn [vars] (apply op (mapv #(% vars) args)))))

(def add (make-op +))
(def subtract (make-op -))
(def multiply (make-op *))
(def divide (make-op #(/ (double %1) %2)))
(def negate (make-op -))
(def negate (make-op -))
(def max (make-op clojure.core/max))
(def min (make-op clojure.core/min))

(defn variable [name] (fn [vars] (vars name)))
(defn constant [value] (constantly value))

(def ops {'+      add, '- subtract, '* multiply, '/ divide
          'negate negate, 'max max, 'min min})

(defn parse-exp [exp]
  (cond
    (number? exp) (constant exp)
    (symbol? exp) (variable (str exp))
    (list? exp) (let [[head & args] exp, op (ops head)]
                   (apply op (map parse-exp args)))))

(defn parseFunction [string]
  (parse-exp (read-string string)))

(defn constant [value]
  (fn [arguments] value))

(defn variable [name]
  (fn [arguments] (arguments name)))

;;Object

(defn constructor [ctor methods] (fn [& args] (apply (partial ctor {:methods methods}) args)))

(defn diff [this name] (((this :methods) :differentiate) this name))
(defn evaluate [this args] (((this :methods) :evaluate) this args))
(defn toString [this] (((this :methods) :toString) this))


(defn ConstVar [this value_name]
  (assoc this
    :value_name value_name))


(def Constant (constructor ConstVar
                           {:evaluate (fn [this args] (this :value_name))
                            :differentiate (fn [this name] (Constant 0))
                            :toString (fn [this] (str (format "%.1f" (this :value_name))))}))



(def Variable (constructor ConstVar
                           {:evaluate (fn [this args] (args (this :value_name)))
                            :differentiate (fn [this name] (if (= (this :value_name) name) (Constant 1) (Constant 0)))
                            :toString (fn [this] (this :value_name))}))

(defn getOperator [f name diff]
  {:evaluate (fn [this args] (apply f (map
                                        (fn [x] (evaluate x args))
                                        (this :operands))))
   :toString (fn [this] (str "(" (clojure.string/join " " (cons name (map
                                                                       (fn [x] (toString x))
                                                                       (this :operands)))) ")"))
   :differentiate (fn [this name] (diff (this :operands) name))
   })

(defn Operation [this & operands]
  (assoc this
    :operands operands))

(def Add (constructor Operation (getOperator + "+"
                                             (fn [f s] (apply Add (map (fn [x] (diff x s)) f))))))
(def Subtract (constructor Operation (getOperator - "-"
                                                  (fn [f s] (apply Subtract (map (fn [x] (diff x s)) f))))))
(def Multiply (constructor Operation (getOperator * "*"
                                                  (defn mult-diff [f s] (cond (empty? (rest f)) (diff (first f) s) :else (Add (apply Multiply (cons (diff (first f) s) (rest f))) (Multiply (first f) (mult-diff (rest f) s))))))))
(def Divide (constructor Operation (getOperator (fn [x y] (/ x (double y))) "/"
                                                (fn [f s] (Divide (Subtract (Multiply (diff (first f) s) (second f)) (Multiply (diff (second f) s) (first f))) (Multiply (second f) (second f)))))))
(def Negate (constructor Operation (getOperator - "negate"
                                                (fn [f s] (Negate (diff (first f) s))))))

(defn parse [operators c v] (defn parseImpl [item] (cond
                                                     (number? item) (c item)
                                                     (symbol? item) (v (str item))
                                                     (list? item) (apply
                                                                    (operators (first item))
                                                                    (map parseImpl (rest item)))
                                                     )))

(def objectOperators {'+ Add '- Subtract '* Multiply '/ Divide 'negate Negate})

(defn parseObject [s] ((parse objectOperators Constant Variable) (read-string s)))