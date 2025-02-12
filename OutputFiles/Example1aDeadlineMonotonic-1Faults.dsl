WARP program for graph Example1A
Scheduler Name: DeadlineMonotonic
numFaults: 1
M: 0.9
E2E: 0.99
nChannels: 16
Time Slot	A	B	C
0	if has(F0) push(F0: A -> B, #9)	wait(#9)	sleep
1	wait(#14)	if has(F0) push(F0: B -> C, #14) else pull(F0: A -> B, #14)	wait(#14)
2	sleep	if has(F0) push(F0: B -> C, #15)	wait(#15)
3	sleep	wait(#5)	if has(F1) push(F1: C -> B, #5)
4	wait(#2)	if has(F1) push(F1: B -> A, #2) else pull(F1: C -> B, #2)	wait(#2)
5	wait(#3)	if has(F1) push(F1: B -> A, #3)	sleep
6	sleep	sleep	sleep
7	sleep	sleep	sleep
8	sleep	sleep	sleep
9	sleep	sleep	sleep
10	if has(F0) push(F0: A -> B, #11)	wait(#11)	sleep
11	wait(#0)	if has(F0) push(F0: B -> C, #0) else pull(F0: A -> B, #0)	wait(#0)
12	sleep	if has(F0) push(F0: B -> C, #1)	wait(#1)
13	sleep	sleep	sleep
14	sleep	sleep	sleep
15	sleep	sleep	sleep
16	sleep	sleep	sleep
17	sleep	sleep	sleep
18	sleep	sleep	sleep
19	sleep	sleep	sleep
// All flows meet their deadlines
