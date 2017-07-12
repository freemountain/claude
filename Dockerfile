FROM mhart/alpine-node:8

WORKDIR /app
ADD package.json package.json
ADD yarn.lock yarn.lock
RUN yarn install --prod
ADD . .
EXPOSE 3000
CMD ["node", "bin/claude"]