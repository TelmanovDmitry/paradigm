init_prime_table(I, LR) :-
  member(I, LR),
  prime(I),
  assert(prime_table(I)).

init(N) :-
    SQRT is ceiling(sqrt(N)) + 2,
    range_list(2, SQRT, LR),
    findall(I, init_prime_table(I, LR), R).

min_divisor(N, P) :-
    prime_table(P),
    P * P =< N,
    0 is N mod P,
    !.

min_divisor(N, N).

prime(N) :-
    min_divisor(N, P),
    N is P.

composite(N) :-
    \+(prime(N)).

range_list(L, L, []).
range_list(N, L, [N | T]) :-
    N < L,
    N1 is N + 1,
    range_list(N1, L, T).

prime_divisors(1, []).
prime_divisors(N, [H | T]) :-
    number(N), N > 1,
    min_divisor(N, H),
    N1 is div(N, H),
    prime_divisors(N1, T).

is_sorted([H]).
is_sorted([H1, H2 | T]) :-
    H1 =< H2,
    is_sorted([H2 | T]).

prime_divisors_list(N, [H | T]) :-
    prime(H),
    prime_divisors(R, T),
    N is R * H.

prime_divisors(N, P) :-
    \+(number(N)),
    is_sorted(P),
    prime_divisors_list(N, P).

merge(X, [], X) :- !.
merge([], Y, Y) :- !.

merge([H | T1], [H | T2], [H | T]) :-
    merge(T1, T2, T).

merge([H1 | T1], [H2 | T2], [H1 | L]) :-
    H1 < H2,
    merge(T1, [H2 | T2], L).

merge([H1 | T1], [H2 | T2], [H2 | L]) :-
    H1 > H2,
    merge([H1 | T1], T2, L).

lcm(X, Y, L) :-
    prime_divisors(X, L1),
    prime_divisors(Y, L2),
    merge(L1, L2, R),
    prime_divisors(L, R).