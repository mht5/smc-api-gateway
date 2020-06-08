def PROJECT_PATH = "C:\\mohan\\Workspace\\Else\\stock-market-charting\\api-gateway"

pipeline {
    agent {label 'master'}
	stages {
	    stage('build') {
	    	steps {
				bat """
					echo 'building jar...';
				    cd ${PROJECT_PATH}
	        		mvn clean install -DskipTests
					echo 'building docker image...';
	        		docker build -t api-gateway .
				"""
	    	}
	    }
	    
	    stage('deploy') {
	    	steps {
	     		echo 'deploying image...';
	        	bat 'docker run -p 127.0.0.1:1010:1010 api-gateway'
	    	}
	    }
	}
}