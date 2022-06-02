import socket
import threading
from tcp_by_size import send_with_size, recv_by_size
import SQL_ORM
import hashlib
import string
import random
from datetime import datetime
import smtplib
import ssl
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

IP = "0.0.0.0"
PORT = 8820
EXIT = False
SENDER_EMAIL = "1@gmail.com"
SENDER_EMAIL_PASSWORD = "***"
MINIMUM_AGE_IN_DAYS = 6026  # 16 and a half years!


def send_email(receiver_email, text, subject):
    """sends an email"""
    message = MIMEMultipart("alternative")
    message["Subject"] = subject
    message["From"] = SENDER_EMAIL
    message["To"] = receiver_email
    message.attach(MIMEText(text, "plain"))  # The email client will try to render the last part first

    # Create secure connection with server and send email
    context = ssl.create_default_context()
    try:
        with smtplib.SMTP_SSL("smtp.gmail.com", 465, context=context) as server:
            server.login(SENDER_EMAIL, SENDER_EMAIL_PASSWORD)
            server.sendmail(SENDER_EMAIL, receiver_email, message.as_string())
            server.close()
    except Exception as e:
        # Print any error messages to stdout
        print(e)
        return False
    else:
        return True


def saltedHash(password, salt):
    """gets a password and a salt and returns the salted hash password"""
    db_pass = password + salt
    hash_pass = hashlib.sha256(db_pass.encode())
    return hash_pass.hexdigest()


def is_age_ok(birthdate):
    """receives the birthdate:%d.%m.%Y and returns true if the user is above the minimum age"""
    my_date = datetime.strptime(birthdate, "%d.%m.%Y")
    today = datetime.now()
    time_difference = (today-my_date).days  # in days
    return time_difference >= MINIMUM_AGE_IN_DAYS


def do_action(data, db):
    """check what client ask and fill the variable "to send" with the answer"""  # according to the protocol
    fields = data.split('|')
    command = fields[0]

    if command != '':  # in cast the list is: ['']
        print(fields)

    to_send = ""

    if command == "LOGIN":
        email = fields[1]

        if not db.check_email_already_exist(email):  # check if there is such email
            to_send = "ERROR|2|Login error-incorrect details"

        else:
            password = fields[2]
            id = db.get_id(email)
            dbData = db.get_hashedPass_salt(id)
            if saltedHash(password, salt=dbData[1]) == dbData[0]:  # check if the password is correct
                table = db.get_role(email) + "s"  # role+s= table's name
                db.update_lastEntry(id, datetime.today().strftime("%d.%m.%Y"), table)
                to_send = "LOGINDATA|" + db.get_name_id_role(email)
            else:
                to_send = "ERROR|2|Login error-incorrect details"

    elif command == "REGISTER":
        email = fields[1]
        password = fields[2]
        name = fields[3]
        role = fields[4]
        birthdate = fields[5]
        id = fields[6]
        phone_num = fields[7]
        address = fields[8]
        if db.check_email_already_exist(email) or db.check_id_already_exist(id):  # check if the details already exist
            to_send = "ERROR|1|Register error-details already exist"

        elif not is_age_ok(birthdate):
            to_send = "ERROR|9|Register error- user must be at least 16.5 years old"

        else:
            # sending the temp password to the email:
            temp = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for i in range(6))

            subject = "Confirm Email"
            text = """\
                        Hi,
                        Here is your temporary password for confirming your email: %s""" % temp

            if send_email(email, text, subject):  # check if the message was sent to the email
                # insert the new Teacher/Student (assuming they are fine)
                salt = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for i in range(16))
                hashed_pass = saltedHash(password, salt)
                db.insert_new_password(SQL_ORM.Password(id, hashed_pass, salt))
                today_date = datetime.today().strftime("%d.%m.%Y")

                if role == "Teacher":
                    teacher = SQL_ORM.Teacher(email, id, name, "", birthdate, phone_num, "", address, "", "False", "False", today_date, today_date)
                    db.insert_new_teacher(teacher)
                else:
                    student = SQL_ORM.Student(email, id, "", name, birthdate, address, phone_num, today_date, today_date)
                    db.insert_new_student(student)

                db.update_pass(id, temp)
                to_send = "ACK"
            else:
                to_send = "ERROR|7|Temp error-can’t send an email"

    elif command == "TEMP":
        email = fields[1]
        temp = fields[2]
        password = fields[3]

        name = db.get_name_id_role(email).split("|")[0]
        id = db.get_id(email)
        role = db.get_role(email)
        db_data = db.get_hashedPass_salt(id)  # (hashedPass, salt)

        # check that the password that sent to the email are equal
        if temp == db_data[0]:
            db.update_pass(id, saltedHash(password, salt=db_data[1]))  # the accurate password

            if role == "Teacher":
                # sending a notification to the admin:
                content = "%s wants to register as a teacher.\nID:%s" % (name, id)
                sendingTime = datetime.today().strftime("%d.%m.%Y %H:%M")
                admin_id = db.get_admin_id()
                db.insert_new_notification(SQL_ORM.Notification(id, admin_id, command, content, "", "", sendingTime))

            to_send = "LOGINDATA|" + db.get_name_id_role(email)
        else:
            # deletes the user's data- the password was incorrect
            db.delete_password(id)
            if role == "Teacher":
                db.delete_teacher(id)
            else:
                db.delete_student(id)
            to_send = "ERROR|6|TEMP-Incorrect password"

    elif command == "TIMEOUT":
        """deletes the user"""
        email = fields[1]
        id = db.get_id(email)
        role = db.get_role(email)

        db.delete_password(id)
        if role == "Teacher":
            db.delete_teacher(id)
        else:
            db.delete_student(id)

        to_send = "DELETED"

    elif command == "INFO":
        id = fields[1]
        car = SQL_ORM.Car(id, car_type=fields[7], year=fields[5], model=fields[4], category_size=fields[6])
        db.insert_new_car(car)
        db.update_teacher_info(id, area=fields[2], seniority=fields[3], price=fields[8])
        to_send = "ACK"

    elif command == "FORGOT":
        email = fields[1]  # receiver_email
        if db.check_email_already_exist(email):
            id = db.get_id(email)
            temp = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for i in range(6))  # need to send to email
            db.update_pass(id, temp)

            subject = "New Temporary Password"
            text = """\
            Hi,
            Here is your new temporary password: %s""" % temp
            if send_email(email, text, subject):  # check if the email sent
                to_send = "ACK"
            else:
                to_send = "ERROR|7|Forgot error-can’t send an email"
        else:
            to_send = "ERROR|3|Forgot error-no such email"

    elif command == "NEWPASS":
        id = db.get_id(email=fields[1])
        db_data = db.get_hashedPass_salt(id)  # (hashedPass, salt)
        temp = fields[2]
        new_pass = fields[3]
        if temp == db_data[0]:  # check that the password that sent to the email are equal
            db.update_pass(id, saltedHash(new_pass, salt=db_data[1]))
            table = db.get_role(email=fields[1]) + "s"  # role+s= table's name
            db.update_lastEntry(id, datetime.today().strftime("%d.%m.%y"), table)
            to_send = "LOGINDATA|" + db.get_name_id_role(email=fields[1])
        else:
            to_send = "ERROR|6|New Password error-no such password"

    elif command == "PROFILE":
        role = fields[1]
        id = fields[2]
        # check if a teacher isn't accepted
        if role == "Teacher" and db.check_if_accepted(id) == "False":
            to_send = "NEEDACCEPT"
        else:
            to_send = "PROFILEDATA|"+db.get_profile(id, role)

    elif command == "STUDENTS":
        if db.check_if_accepted(teacherId=fields[1]) == "False":
            to_send = "NEEDACCEPT"
        else:
            to_send = "STUDENTSDATA" + db.get_students(teacherId=fields[1])

    elif command == "STUDENTLESSONS":
        to_send = "STUDENTLESSONSDATA" + db.get_student_lessons(studentId=fields[1])

    elif command == "DAY":
        if db.check_if_accepted(teacherId=fields[1]) == "False":
            to_send = "NEEDACCEPT"
        else:
            date = fields[2]
            to_send = "DAYDATA|" + date + db.get_day_data(teacherId=fields[1], date=fields[2])

    elif command == "NOTIFICATIONS":
        receiver_id = fields[1]
        # if it's a teacher that didn't got accepted by the admin:
        if db.check_value_already_exist("Teachers", "Id", receiver_id) and db.check_if_accepted(teacherId=receiver_id) == "False":
            to_send = "NEEDACCEPT"
        else:
            to_send = "NOTIFICATIONSDATA" + db.get_notifications(receiver_id)

    elif command == "PAYMENT":
        db.update_paid_status(date=fields[1], time=fields[2], studentId=fields[3], curr_paid_status=fields[4])
        to_send = "ACK"

    elif command == "FREE":
        student_id = fields[1]
        date = fields[2]
        teacher_id = db.get_teacherId_by_studentId(student_id)
        if teacher_id == '':  # the student doesn't have a teacher
            to_send = "NEEDTEACHER"
        else:
            to_send = "FREEDATA|" + date + "|" + teacher_id + db.get_student_day_data(student_id, teacher_id, date)

    elif command == "LESSONREQUEST":
        date = fields[1]
        time = fields[2]
        studentId = fields[3]
        teacherId = fields[4]
        location = fields[5]
        student_name = db.get_student_name(studentId)
        content = "%s wants to date a lesson \non %s %s at %s" % (student_name, date, time, location)
        sendingTime = datetime.today().strftime("%d.%m.%Y %H:%M")
        notification_data = data.replace(command+"|", "")

        # before adding the notification - checks if it already exists:
        if not db.check_request_already_exist(senderId=studentId, receiverId=teacherId, type=command, data=notification_data):
            db.insert_new_notification(SQL_ORM.Notification(studentId, teacherId, command, content, notification_data, "", sendingTime))
        to_send = "ACK"

    elif command == "JOINREQUEST":
        studentId = fields[1]
        teacherId = fields[2]

        if db.get_teacherId_by_studentId(studentId) != "":
            to_send = "ERROR|8|Join error-student already has a teacher"

        else:
            student_name = db.get_student_name(studentId)
            student_profile = db.get_student_profile(studentId).split("|")
            address = student_profile[1]
            phone = student_profile[2]
            content = "%s wants to join. Address:\n%s. Phone:%s " % (student_name, address, phone)
            sendingTime = datetime.today().strftime("%d.%m.%Y %H:%M")
            notification_data = data.replace(command + "|", "")

            # before adding the notification - checks if it already exists:
            if not db.check_request_already_exist(senderId=studentId, receiverId=teacherId, type=command, data=notification_data):
                db.insert_new_notification(SQL_ORM.Notification(studentId, teacherId, command, content, notification_data, "", sendingTime))
            to_send = "ACK"

    elif command == "CANCELREQUEST":
        senderId = fields[1]
        receiverId = fields[2]
        date = fields[3]
        startTime = fields[4]

        sender_name = db.get_user_name(senderId)
        content = "%s wants to cancel the \nlesson on %s %s" % (sender_name, date, startTime)
        sendingTime = datetime.today().strftime("%d.%m.%Y %H:%M")
        notification_data = data.replace(command+"|", "")

        # before adding the notification - checks if it already exists:
        if not db.check_request_already_exist(senderId, receiverId, type=command, data=notification_data):
            db.insert_new_notification(SQL_ORM.Notification(senderId, receiverId, command, content, notification_data, "", sendingTime))
        to_send = "ACK"

    elif command == "CANCELALLREQUEST":
        teacherId = fields[1]
        date = fields[2]

        # sending requests for each student
        for i in range(3, len(fields)):
            lesson_data = fields[i].split("#")
            receiverId = lesson_data[0]
            startTime = lesson_data[1]
            sender_name = db.get_user_name(teacherId)

            content = "%s wants to cancel the lesson \non %s %s" % (sender_name, date, startTime)
            sendingTime = datetime.today().strftime("%d.%m.%Y %H:%M")
            notification_data = teacherId + "|" + receiverId + "|" + date + "|" + startTime  # like cancel request

            # before adding the notification - checks if it already exists:
            if not db.check_request_already_exist(teacherId, receiverId, type=command, data=notification_data):
                db.insert_new_notification(SQL_ORM.Notification(teacherId, receiverId, "CANCELREQUEST", content, notification_data, "", sendingTime))

        to_send = "ACK"

    elif command == "NOTIFICATIONSTATUS":
        senderId = fields[1]
        receiverId = fields[2]
        sendingTime = fields[3]
        status = fields[4]

        db.update_notification_status(senderId, receiverId, sendingTime, status)
        type, notification_data = db.get_notification_type_data(senderId, receiverId, sendingTime)
        notification_data = notification_data.split("|")
        print("The notification data: ", notification_data)

        send_time = datetime.today().strftime("%d.%m.%Y %H:%M")
        to_send = "ACK"  # if there is a change- changing in the conditionals

        if status == "True":
            if type == "LESSONREQUEST":  # add the new lesson and sending back a notification to the sender
                # check if available - if not: not accepting the request
                if db.check_lesson_is_available(teacherId=receiverId, date=notification_data[0], startTime=notification_data[1]):
                    db.insert_new_lesson(SQL_ORM.Lesson(student_id=senderId, teacher_id=receiverId, date=notification_data[0], startTime=notification_data[1], price=db.get_teacher_price(receiverId), duration=60, pick_up_location=notification_data[4], isPaid=False))
                    content = "The lesson on %s at %s \nfrom %s is set!" % (notification_data[0], notification_data[1], notification_data[4])

                else:  # lesson already occupied!!
                    db.update_notification_status(senderId, receiverId, sendingTime, "False")
                    content = "The request for a lesson on %s at %s \nfrom %s was not accepted!" % (notification_data[0], notification_data[1], notification_data[4])
                    to_send = "OCCUPIED"
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))

            elif type == "JOINREQUEST":
                db.update_student_teacher(studentId=senderId, teacherId=receiverId)
                db.delete_join_requests(studentId=senderId, teacherId=receiverId)  # if the student asked another teachers
                teacher_name = db.get_user_name(id=receiverId)
                content = "Your request to join to \n%s has been approved!" % teacher_name
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))

            elif type == "CANCELREQUEST":
                db.delete_lesson(senderId, receiverId, date=notification_data[2], startTime=notification_data[3])
                content = "Your request to cancel the lesson on \n%s %s has been approved!" % (notification_data[2], notification_data[3])
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))

            elif type == "REGISTER":
                db.update_teacher_accepted(teacherId=senderId)
                admin_name = db.get_user_name(id=receiverId)
                content = "Your request to register as a teacher has \nbeen approved by %s!" % admin_name
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))
        else:
            if type == "LESSONREQUEST":
                content = "The request for a lesson on %s at %s \nfrom %s was not accepted!" % (notification_data[0], notification_data[1], notification_data[4])
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))

            elif type == "JOINREQUEST":
                teacher_name = db.get_user_name(id=receiverId)
                content = "Your request to join to %s \nhas not been approved!" % teacher_name
                db.insert_new_notification(SQL_ORM.Notification(receiverId, senderId, "ANSWER", content, "", "", send_time))

            elif type == "REGISTER":  # deletes the teacher
                db.delete_teacher(senderId)
                db.delete_password(senderId)
                db.delete_notification(senderId)

    elif command == "TEACHERS":
        to_send = "TEACHERSDATA" + db.get_accepted_teachers()

    elif command == "DELETENOTIFICATION":
        db.delete_notification(senderId=fields[1], receiverId=fields[2], sendingTime=fields[3])
        to_send = "ACK"

    return to_send


def handle_client(client_socket, db):
    global EXIT
    while not EXIT:
        try:
            data = recv_by_size(client_socket)
            if data == "":
                print("Error: Seems Client DC-need to close the socket")
                break

            to_send = do_action(data.decode(), db)
            send_with_size(client_socket, to_send.encode())

        except socket.error as err:
            if err.errno == 10054:  # 'Connection reset by peer'
                print("Error %d Client is Gone. %s reset by peer." % (err.errno, str(client_socket)))
                break
            else:
                print("Client disconnected or on resume-%d General Sock Error" % err.errno)
                break

        except Exception as err:
            print("General Error:", err)
            break

    client_socket.close()


def main():
    global EXIT
    srv_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv_socket.bind((IP, PORT))
    srv_socket.listen()

    db = SQL_ORM.DriveORM()
    threads = []
    while not EXIT:
        client_socket, address = srv_socket.accept()
        t = threading.Thread(target=handle_client, args=(client_socket, db))
        t.start()
        threads.append(t)

    for t in threads:
        t.join()


if __name__ == "__main__":
    main()
