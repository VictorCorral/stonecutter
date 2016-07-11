#!/usr/bin/env bash

ssh -i ~/.ssh/stonecutter_dob_private_key $REMOTE_USER@$SERVER_IP <<EOF
  docker stop stonecutter || echo 'Failed to stop stonecutter container'
  docker rm stonecutter || echo 'Failed to remove stonecutter container'
  docker rmi dcent/stonecutter
  docker run -d -v /var/stonecutter/config:/var/config \
             --env-file=/var/stonecutter/config/stonecutter.env \
             -v /data/stonecutter/static \
             -v /var/stonecutter/email_service/providers/mailgun:/var/stonecutter/email_service/providers/mailgun \
             -v /var/stonecutter/email_service/send_mail.sh:/var/stonecutter/email_service/send_mail.sh \
             -p 5000:5000 \
             --restart=on-failure \
             --link mongo:mongo --name stonecutter dcent/stonecutter
EOF