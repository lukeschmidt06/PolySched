WARP program for graph Example1A
Scheduler Name: RealtimeHART
numFaults: 1
M: 0.9
E2E: 0.99
nChannels: 16
Time Slot	A	B	C
0	push(F0: A -> B, #13)	wait(#13)	sleep
1	push(F0: A -> B, #14)	wait(#14)	sleep
2	sleep	push(F0: B -> C, #4)	wait(#4)
3	sleep	push(F0: B -> C, #5)	wait(#5)
4	sleep	wait(#7)	push(F1: C -> B, #7)
5	sleep	wait(#8)	push(F1: C -> B, #8)
6	wait(#9)	push(F1: B -> A, #9)	sleep
7	wait(#10)	push(F1: B -> A, #10)	sleep
8	sleep	sleep	sleep
9	sleep	sleep	sleep
10	push(F0: A -> B, #15)	wait(#15)	sleep
11	push(F0: A -> B, #0)	wait(#0)	sleep
12	sleep	push(F0: B -> C, #6)	wait(#6)
13	sleep	push(F0: B -> C, #7)	wait(#7)
14	sleep	sleep	sleep
15	sleep	sleep	sleep
16	sleep	sleep	sleep
17	sleep	sleep	sleep
18	sleep	sleep	sleep
19	sleep	sleep	sleep
// All flows meet their deadlines
