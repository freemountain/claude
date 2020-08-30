# Claude


## Commands

### Create Cluster
```
./bin/k3d cluster create demo --agents 1 -p "30000-30080:30000-30080@agent[0]" --volume "$PWD"/etc/registries.yaml:/etc/rancher/k3s/registries.yaml
```

### Run the local registry
```
docker container run -d --name registry.local -v registry:/var/lib/registry --restart always -p 5000:5000 registry:2
# you can push images to localhost:5000 now

# connect the registry to the cluster
docker network connect k3d-demo registry.local
```

### Create volume hostpath
```
docker exec -it k3d-demo-agent-0 sh -c 'mkdir -p /var/volumes/NAME'
```

### Get a mysql shell
```
mysql -h 127.0.0.1 -P 30079 -ppassword -u root
```