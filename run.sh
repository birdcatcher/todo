if [ $# -eq 0 ]
	then
		echo "./run.sh port"
		java -cp .:lib/* AppServer 8080		
	else
		java -cp .:lib/* AppServer $1		
fi
