# The-Huxley-Evaluator

* Importe o projeto no IDEA
* Clique com o botão direito em pom.xml e selecione "Add as Maven"
* Se for necessário, altere as propriedades do arquivo pom.xml no devido profile.

* O profile padrão é o dev, caso queria utilizar o prod, basta adicionar a opção -Denv=prod.

* Exemplo: mvn clean package -Denv=prod

# Script is_alive.py

* É um script que verifica se o avaliador está rodando. Esta verificação é feita através da quantidade de consumers das filas submission_queue e oracle_queue. Deverá sempre haver ao menos um consumer escutando em cada das filas.

* O script deve ser execudado como sudo e com python 2.7.



https://www.digitalocean.com/community/tutorials/how-to-set-up-an-nfs-mount-on-ubuntu-14-04

# servidor

sudo apt-get install nfs-kernel-server
/home/marcio/dev/code/huxley/testcases 10.0.2.15(ro,sync)
sudo exportfs -a
sudo service nfs-kernel-server start

# cliente

sudo apt-get install nfs-common
sudo mkdir -p /mnt/nfs/var/testcases
sudo mount 104.237.135.224:/home/marcio/dev/code/huxley/testcases /mnt/nfs/var/testcases


#cache

sudo apt-get install cachefilesd
edit fstab includ
edit /etc/default/cachefilesd and set RUN=yes
sudo /etc/init.d/cachefilesd start

https://askubuntu.com/questions/4572/how-can-i-cache-nfs-shares-on-a-local-disk
