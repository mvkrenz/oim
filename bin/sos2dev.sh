#!/bin/bash

#get oim data from sonofsam
ssh sonofsam "cd /usr/local/rsv-gratia-collector-0.27.5 && . setup.sh && \$VDT_LOCATION/mysql5/bin/mysqldump -u oim-reader --password=w1llyw0nk --databases oimnew" | sed '1d' | sed 's/oimnew/oim2/' | mysql -u root

