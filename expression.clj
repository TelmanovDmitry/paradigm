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

(defn parse-sexp [sexp]
  (cond
    (number? sexp) (constant sexp)
    (symbol? sexp) (variable (str sexp))
    (list? sexp) (let [[head & args] sexp, op (ops head)]
                   (apply op (map parse-sexp args)))))

(defn parseFunction [string]
  (parse-sexp (read-string string)))

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

(def Square (constructor Operation (getOperator (fn [x] ( * x x)) "square"
                                                (fn [f s] (Multiply (Multiply (Constant 2)) (diff (first f) s))))))

(def Sqrt (constructor Operation (getOperator (fn [x] (Math/sqrt(Math/abs ^double x))) "sqrt"
                                              (fn [f s] (Divide
                                                          (Multiply s f)
                                                          (Multiply (Constant 2) (Sqrt (Multiply (Square s) s))))))))


(def Lg (constructor Operation (getOperator
                                 (fn [x y] (/ (Math/log (Math/abs ^double y)) (Math/log (Math/abs ^double x)))) "lg"
                                 (fn [f s t v] (Subtract
                                                 (Divide v (Multiply s (Lg (Constant Math/E ) f)))
                                                 (Divide
                                                   (Multiply (Lg (Constant Math/E) s) t)
                                                   (Multiply f
                                                             (Square
                                                               (Lg (Constant Math/E) f)))))))))

(def Pw (constructor Operation (getOperator (fn [x y] (Math/pow x y)) "pw"
                                            (fn [f s t v] (Add
                                                            (Multiply
                                                              (Multiply s
                                                                        (Pw f
                                                                            (Subtract s (Constant 1)))) t)
                                                            (Multiply
                                                              (Multiply
                                                                (Pw f s)
                                                                (Lg (Constant Math/E) f)) v))))))




(def And (constructor Operation (getOperator
                                  (fn [x y]
                                    (Double/longBitsToDouble
                                      (bit-and (Double/doubleToLongBits x) (Double/doubleToLongBits y))))
                                  "&" (fn [f s t v] (Constant 0)))))


(def Or (constructor Operation (getOperator
                                 (fn [x y]
                                   (Double/longBitsToDouble
                                     (bit-or (Double/doubleToLongBits x) (Double/doubleToLongBits y)))) "|"
                                 (fn [f s t v] (Constant 0)))))

(def Xor (constructor Operation (getOperator
                                  (fn [x y]
                                    (Double/longBitsToDouble
                                      (bit-xor (Double/doubleToLongBits x) (Double/doubleToLongBits y)))) "^"
                                  (fn [f s t v] (Constant 0)))))


(defn parse [operators c v] (defn parseImpl [item] (cond
                                                     (number? item) (c item)
                                                     (symbol? item) (v (str item))
                                                     (list? item) (apply
                                                                    (operators (first item))
                                                                    (map parseImpl (rest item)))
                                                     )))

(def objectOperators {'+ Add '- Subtract '* Multiply '/ Divide 'negate Negate
                      'square Square 'sqrt Sqrt 'pw Pw 'lg Lg})

(defn parseObject [s] ((parse objectOperators Constant

                              Variable) (read-string s)))