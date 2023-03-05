# gitlet
A limited version control system mimicking a few basic features of Git

Features:
1. add
2. commit
3. init (initialise .git dir)
4. rm: removes file from index and untracks it. only reverseable when user adds file again.
5. log: shows details about current branch's commit until the very first commit.
6. glog: shows details about all commits
7. find: find commits with the same log message.
8. status: displays what branches currently exist, files staged/ untracked/ removed, and marks the current branch with a *. 
9. checkout: checkout file from commit ID (CMD is: checkout commitID file.txt)
10. coutID (checkoutID) : checkout the snapshot captured at this commitID.
11. coutb : transfer user to this branch
12. merge: merge one branch into main master branch
13. rth (return to head) : transfer user to master branch.
14. seen: make .git directory visible to user
15. hidden: make .git directory hidden to user
16. exit: exit the system.
 
