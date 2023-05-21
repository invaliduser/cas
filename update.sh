cd /root/cas 
git pull 
npm install 
clj -X:dev user/compile-once 
clj -M:prod
