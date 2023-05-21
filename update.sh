cd /root/cas 
git pull 
npm install 
shadow-cljs release :app
clj -M:prod
