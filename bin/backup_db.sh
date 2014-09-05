#!/bin/bash
date=`date +%Y-%m-%d`
echo $date >> /var/log/backup.log
mysqldump -u root --databases oim --master-data | gzip -c | ssh -i /root/.ssh/id_goc.dsa goc@backup.goc "cd /usr/local/backup/oim/${HOSTNAME} && cat > oim.sql.$date.gz && ln -sf oim.sql.$date.gz oim.sql.latest.gz"

