WARP program for graph Example1A
Scheduler Name: RateMonotonic
numFaults: 1
M: 0.9
E2E: 0.99
nChannels: 16
Time Slot	A	B	C
0	if has(F0) push(F0: A -> B, #5)	wait(#5)	sleep
1	wait(#8)	if has(F0) push(F0: B -> C, #8) else pull(F0: A -> B, #8)	wait(#8)
2	sleep	if has(F0) push(F0: B -> C, #9)	wait(#9)
3	sleep	wait(#3)	if has(F1) push(F1: C -> B, #3)
4	wait(#12)	if has(F1) push(F1: B -> A, #12) else pull(F1: C -> B, #12)	wait(#12)
5	wait(#13)	if has(F1) push(F1: B -> A, #13)	sleep
6	sleep	sleep	sleep
7	sleep	sleep	sleep
8	sleep	sleep	sleep
9	sleep	sleep	sleep
10	if has(F0) push(F0: A -> B, #7)	wait(#7)	sleep
11	wait(#10)	if has(F0) push(F0: B -> C, #10) else pull(F0: A -> B, #10)	wait(#10)
12	sleep	if has(F0) push(F0: B -> C, #11)	wait(#11)
13	sleep	sleep	sleep
14	sleep	sleep	sleep
15	sleep	sleep	sleep
16	sleep	sleep	sleep
17	sleep	sleep	sleep
18	sleep	sleep	sleep
19	sleep	sleep	sleep
// All flows meet their deadlines
