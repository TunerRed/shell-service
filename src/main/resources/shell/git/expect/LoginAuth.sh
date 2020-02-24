set GIT_USER [ lindex $argv 0 ]
set GIT_PASS [ lindex $argv 1 ]
set SHELLNAME [ lindex $argv 2 ]

spawn sh $SHELLNAME

expect "*sername" {send "$GIT_USER\n"}
expect {
"*assword" {send "$GIT_PASS\n"}
"yes" {
  send "yes\n"
  expect "*assword" {send "$GIT_PASS\n"}
}
}

interact

