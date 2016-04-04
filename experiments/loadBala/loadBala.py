#!/usr/bin/env python
import numpy as np
import matplotlib.pyplot as plt


##### How to create the vo1,vo2,vo3 txt files #####
### BASH COMMANDS ###
# mkdir debugs
# grep DEBUG  <gslogfile.log> >> debugs/<vofile.txt>

### Python commands (could be run in a shell) ###
#with open("vo1.txt",'r') as f:
#	l1 = map(lambda x (x.split(' ')[-1].split(',')[-3],x.split(' ')[-1].split(',')[-1].strip('\n')),f.readlines())

#vo1 = map(lambda x: int(x[0])+int(x[1]),l1)	


# vo1 contains the number of jobs running per second in the VO1
# vo2 containes the number of jobs running per second in the Vo2
# vo3 contains the number of jobs running per second in the vo3

vo1 = np.load("v01.npy")
vo2 = np.load("v02.npy")
vo3 = np.load("v03.npy")

setting = {"color":"navy","style":"italic"}
fig = plt.figure()
xvalues = np.arange(len(vo1))
lines = plt.plot(xvalues,vo1,xvalues,vo2,xvalues,vo3,lw=2)
plt.title("Load per VO for 100 jobs",setting)
plt.xlabel("Seconds (sec)",setting)
plt.ylabel("Load (number of jobs)",setting)
ax, = fig.get_axes()
plt.xlim(0,120)
plt.ylim(0,6)
plt.grid(True)
plt.legend(lines,['VO_1','VO_2','VO_3'])
plt.savefig("loadBala.png")
plt.show()