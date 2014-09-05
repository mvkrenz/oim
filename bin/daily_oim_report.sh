wget -O report.html "http://localhost:8080/oim/reportregistration?plain&days=1"
true > mail.txt
echo "To: goc@opensciencegrid.org" >> mail.txt
echo "From: goc@opensciencegrid.org" >> mail.txt
echo "Subject: [oim] Registration Report" >> mail.txt
echo "Content-Type: text/html; charset=\"utf-8\"" >> mail.txt
echo "" >> mail.txt
echo "<html><head>" >> mail.txt
echo "</head><body>" >> mail.txt
cat report.html >> mail.txt
echo "</body></html>" >> mail.txt

/usr/sbin/sendmail hayashis@indiana.edu < mail.txt

