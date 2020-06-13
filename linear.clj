(defn v+ [a b] (mapv + a b))
(defn v- [a b] (mapv - a b))
(defn v* [a b] (mapv * a b))

(defn v*s [a b] (mapv (fn [n] (* n b)) a))
(defn scalar [a, b] (apply + (v* a b)))
(defn vect [a b]
  [(- (* (a 1) (b 2)) (* (a 2) (b 1)))
   (- (* (a 2) (b 0)) (* (a 0) (b 2)))
   (- (* (a 0) (b 1)) (* (a 1) (b 0)))])

(defn m+ [a b] (mapv v+ a b))
(defn m- [a b] (mapv v- a b))
(defn m* [a b] (mapv v* a b))

(defn m*s [a b] (mapv (fn [n] (v*s n b)) a))
(defn m*v [a b] (mapv (fn [n] (scalar n b)) a))
(defn transpose [m] (apply mapv vector m))
(defn m*m [a b] (mapv (fn [n] (mapv (fn [m] (scalar n m)) (transpose b))) a))

(defn s+ [a b] (if (vector? a) (mapv s+ a b) (+ a b)))
(defn s- [a b] (if (vector? a) (mapv s- a b) (- a b)))
(defn s* [a b] (if (vector? a) (mapv s* a b) (* a b)))