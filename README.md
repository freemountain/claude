# Claude
Your firendly docker budy

## Prerequisites

### DNS

#### OSX
```bash
brew install dnsmasq
IP=$(echo $DOCKER_HOST|egrep -o '([0-9]{1,3}[.]){3}[0-9]{1,3}')
rm -f /usr/local/etc/dnsmasq.conf
echo "address=/.dev/$IP" > /usr/local/etc/dnsmasq.conf
sudo brew services stop dnsmasq
sudo brew services start dnsmasq
dig foo.dev @127.0.0.1
```