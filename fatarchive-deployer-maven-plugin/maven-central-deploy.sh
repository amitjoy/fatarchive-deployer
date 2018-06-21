# Deploy maven artefact in current directory into Maven central repository
# using maven-release-plugin goals
read -p "Really deploy to maven central repository  (yes/no)? "
if ( [ "$REPLY" == "yes" ] ) then
  ssh-add ~/git/github-amit
  ssh-add -l
  mvn release:clean release:prepare release:perform -B -e -X | tee maven-central-deploy.log
  ssh-add -D
else
  echo 'Exit without deploy'
fi