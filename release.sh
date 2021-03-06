lein fig:min

echo "preparing..."
mkdir -p ./dist/cljs-out
mkdir -p ./dist/css
mkdir -p ./dist/assets

echo "copying files..."
cp ./resources/public/cljs-out/dev-main.js ./dist/cljs-out/dev-main.js
cp -r ./resources/public/css ./dist
cp -r ./resources/public/assets ./dist
cp ./resources/public/index.html ./dist

echo "packaging..."
zip -r dist.zip ./dist

echo "uploading..."
butler push dist.zip twopm/frogue:web

echo "done!"