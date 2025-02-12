WARP program for graph Example1A
Scheduler Name: Priority
numFaults: 1
M: 0.9
E2E: 0.99
nChannels: 16
Time Slot	A	B	C
0	if has(F0) push(F0: A -> B, #1)	wait(#1)	sleep
1	wait(#2)	if has(F0) push(F0: B -> C, #2) else pull(F0: A -> B, #2)	wait(#2)
2	sleep	if has(F0) push(F0: B -> C, #3)	wait(#3)
3	sleep	wait(#1)	if has(F1) push(F1: C -> B, #1)
4	wait(#6)	if has(F1) push(F1: B -> A, #6) else pull(F1: C -> B, #6)	wait(#6)
5	wait(#7)	if has(F1) push(F1: B -> A, #7)	sleep
6	sleep	sleep	sleep
7	sleep	sleep	sleep
8	sleep	sleep	sleep
9	sleep	sleep	sleep
10	if has(F0) push(F0: A -> B, #3)	wait(#3)	sleep
11	wait(#4)	if has(F0) push(F0: B -> C, #4) else pull(F0: A -> B, #4)	wait(#4)
12	sleep	if has(F0) push(F0: B -> C, #5)	wait(#5)
13	sleep	sleep	sleep
14	sleep	sleep	sleep
15	sleep	sleep	sleep
16	sleep	sleep	sleep
17	sleep	sleep	sleep
18	sleep	sleep	sleep
19	sleep	sleep	sleep
// All flows meet their deadlines
