import sqlite3
from datetime import datetime

DURATION_LESSON = 60
START_SHIFT = "07:00"
END_SHIFT = "19:00"


class Student(object):
    def __init__(self, email, Id, teacherId, name, birthdate, address, phone_num, lastEntry, createdDate):
        self.email = email
        self.Id = Id
        self.teacherId = teacherId
        self.name = name
        self.birthdate = birthdate
        self.address = address
        self.phone_num = phone_num
        self.lastEntry = lastEntry
        self.createdDate = createdDate


class Teacher(object):
    def __init__(self, email, Id, name, area, birthdate, phone_num, seniority, address, price, isAdmin, isAccepted, lastEntry,createdDate):
        self.email = email
        self.Id = Id
        self.name = name
        self.area = area
        self.birthdate = birthdate
        self.phone_num = phone_num
        self.seniority = seniority
        self.address = address
        self.price = price
        self.isAdmin = isAdmin
        self.isAccepted = isAccepted
        self.lastEntry = lastEntry
        self.createdDate = createdDate


class Car(object):
    def __init__(self, teacherId, car_type, year, model, category_size):
        self.teacherId = teacherId
        self.car_type = car_type
        self.year = year
        self.model = model
        self.category_size = category_size


class Lesson(object):
    def __init__(self, student_id, teacher_id, date, startTime, price, duration, pick_up_location, isPaid):
        self.student_id = student_id
        self.teacher_id = teacher_id
        self.date = date
        self.startTime = startTime
        self.price = price
        self.duration = duration
        self.pick_up_location = pick_up_location
        self.isPaid = isPaid


class Password(object):
    def __init__(self, Id, hashedPass, salt):
        self.Id = Id
        self.hashedPass = hashedPass
        self.salt = salt


class Notification(object):
    def __init__(self, sender_id, receiver_id, type, content, data, status, sendingTime):
        self.sender_id = sender_id
        self.receiver_id = receiver_id
        self.type = type
        self.content = content
        self.data = data
        self.status = status
        self.sendingTime = sendingTime


class DriveORM:
    def __init__(self):
        self.conn = None  # will store the DB connection
        self.cursor = None  # will store the DB connection cursor
        self.current = None

    def open_DB(self):
        """ will open DB file and put value in: self.conn (need DB file name) and self.cursor """
        self.conn = sqlite3.connect('driveApp.db')
        self.current = self.conn.cursor()

    def close_DB(self):
        self.conn.close()

    def commit(self):
        self.conn.commit()

    # All read SQL
    def check_value_already_exist(self, table, column, value):
        """check if a certain value exists in a column in a table"""
        self.open_DB()
        sql = "SELECT COUNT(*) \nFROM %s \nWHERE %s='%s';" % (table, column, value)
        print(sql)
        self.current.execute(sql)
        count = self.current.fetchone()
        self.close_DB()
        if count[0] == 1:
            return True
        return False

    def check_request_already_exist(self, senderId, receiverId, type, data):
        """check if a user have already asked for a request like: JOINREQUEST, CANCELREQUEST, LESSONREQUEST"""
        self.open_DB()
        sql = "SELECT COUNT(*) \nFROM Notifications \nWHERE senderId='%s' AND receiverId='%s' AND type='%s' AND data='%s';" % (senderId, receiverId, type, data)
        print(sql)
        self.current.execute(sql)
        count = self.current.fetchone()
        self.close_DB()
        if count[0] == 1:
            return True
        return False

    def check_lesson_is_available(self, teacherId, date, startTime):
        """check if the requested lesson is available after the teacher's approval
        (not already occupied by another) and returns True/False"""
        self.open_DB()
        sql = "SELECT COUNT(*) \nFROM Lessons \nWHERE teacherId='%s' AND date='%s' AND startTime='%s';" % (teacherId, date, startTime)
        print(sql)
        self.current.execute(sql)
        count = self.current.fetchone()
        self.close_DB()
        if count[0] == 1:
            return False
        return True

    def check_id_already_exist(self, Id):
        """check if the Id already exist in the tables: Students/Teachers."""
        return self.check_value_already_exist("Students", "Id", Id) or self.check_value_already_exist("Teachers", "Id",
                                                                                                      Id)

    def check_email_already_exist(self, email):
        """check if the email already exist in the tables: Students/Teachers."""
        return self.check_value_already_exist("Students", "email", email) or self.check_value_already_exist("Teachers", "email", email)

    def get_name_id_role(self, email):
        """get the name, id, role of the user by email"""
        if self.check_value_already_exist("Students", "email", email):  # check in which table the email exists
            sql = "SELECT name, Id \nFROM Students \nWHERE email='%s';" % email
            role = "Student"
        else:
            sql = "SELECT name, Id \nFROM Teachers \nWHERE email='%s';" % email
            role = "Teacher"
        self.open_DB()
        print(sql)
        self.current.execute(sql)
        data = self.current.fetchone()
        self.close_DB()
        return data[0] + "|" + data[1] + "|" + role  # name|Id|role

    def get_teacherId_by_studentId(self, studentId):
        """returns the student's teacherId"""
        self.open_DB()
        sql = "SELECT teacherId \nFROM Students \nWHERE Id='%s';" % studentId
        print(sql)
        self.current.execute(sql)
        teacherId = self.current.fetchone()[0]
        self.close_DB()
        return teacherId

    def check_if_accepted(self, teacherId):
        """returns true/false if the teacher already got accepted"""
        self.open_DB()
        sql = "SELECT isAccepted \nFROM Teachers \nWHERE Id='%s';" % teacherId
        print(sql)
        self.current.execute(sql)
        is_accepted = self.current.fetchone()[0]
        self.close_DB()
        return is_accepted

    def get_id(self, email):
        """returns the id of the user by email"""
        return self.get_name_id_role(email).split("|")[1]

    def get_role(self, email):
        """returns the role of the user by email"""
        return self.get_name_id_role(email).split("|")[2]

    def get_hashedPass_salt(self, id):
        """returns a tuple- the hashed password and its salt."""
        self.open_DB()
        sql = "SELECT hashedPass, salt \nFROM Passwords \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        data = self.current.fetchone()
        self.close_DB()
        return data  # tuple (hashedPass, salt)

    def get_teacher_car(self, teacherId):
        """returns the teacher's car's details"""
        self.open_DB()
        sql = "SELECT model, year, categorySize, type \nFROM Cars \nWHERE teacherId='%s';" % teacherId
        print(sql)
        self.current.execute(sql)
        car_data = self.current.fetchone()
        self.close_DB()
        return car_data  # tuple

    def get_teacher_profile(self, id):
        """returns the needed data for teacher's profile by id."""
        self.open_DB()
        sql = "SELECT email, area, phoneNum, price \nFROM Teachers \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        data = self.current.fetchone()
        profile = data[0] + "|" + data[1] + "|" + data[2] + "|" + str(data[3]) + "|"  # email|area|phone number|price

        sql = "SELECT model, year \nFROM Cars \nWHERE teacherId='%s';" % id
        print(sql)
        self.current.execute(sql)
        data = self.current.fetchone()
        profile += data[0] + "|" + data[1]  # model|year
        self.close_DB()
        return profile  # data= email|area|phone number|price|model|year

    def get_student_profile(self, id):
        """returns the needed data for student's profile by id."""
        self.open_DB()
        sql = "SELECT email, address, phoneNumber, birthdate, teacherId \nFROM Students \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        data = self.current.fetchone()
        profile = data[0] + "|" + data[1] + "|" + data[2] + "|" + data[3]  # email|address|phone number|birthdate
        teacher_id = data[4]
        self.close_DB()

        if teacher_id != '':
            profile += "|" + teacher_id + "|" + self.get_user_name(teacher_id)  # teacherId|teacherName

        return profile

    def get_profile(self, id, role):
        """returns the profile's data"""
        if role == "Teacher":
            return self.get_teacher_profile(id)
        return self.get_student_profile(id)

    def get_student_debt(self, studentId, teacherId):
        """return the student's debt- for a specific teacher"""
        self.open_DB()
        sql = "SELECT SUM(price) \nFROM Lessons \nWHERE studentId='%s' AND teacherId='%s' AND isPaid='False';" % (
        studentId, teacherId)
        print(sql)
        self.current.execute(sql)
        debt = self.current.fetchone()[0]
        self.close_DB()
        return debt  # int

    def get_students(self, teacherId):
        """returns a list of all students' id,name,address,phone,debt of a specific teacher"""
        self.open_DB()
        sql = "SELECT Id, name, address, phoneNumber \nFROM Students \nWHERE teacherId='%s' \nORDER BY name;" % teacherId
        print(sql)
        self.current.execute(sql)
        students_dict = {}
        for row in self.current:
            student_id = row[0]
            students_dict[student_id] = "|" + row[0] + "#" + row[1] + "#" + row[2] + "#" + row[3]  # the data: |id#name#address#phone

        all_message = ""
        # to add the debt for each student to the dictionary
        for id in students_dict.keys():
            debt = self.get_student_debt(id, teacherId)
            students_dict[id] += ("#" + str(debt))
            all_message += students_dict[id]

        self.close_DB()
        return all_message

    def get_student_name(self, id):
        """returns the name of the student by id"""
        self.open_DB()
        sql = "SELECT name \nFROM Students \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        name = self.current.fetchone()[0]
        self.close_DB()
        return name

    def get_teacher_name(self, id):
        """returns the name of the teacher by id"""
        self.open_DB()
        sql = "SELECT name \nFROM Teachers \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        name = self.current.fetchone()[0]
        self.close_DB()
        return name

    def get_teacher_price(self, id):
        """returns the teacher's price by id"""
        self.open_DB()
        sql = "SELECT price \nFROM Teachers \nWHERE Id='%s';" % id
        print(sql)
        self.current.execute(sql)
        price = self.current.fetchone()[0]
        self.close_DB()
        return price

    def get_user_name(self, id):
        """returns the name of the user by his id"""
        if self.check_value_already_exist("Teachers", "Id", id):
            return self.get_teacher_name(id)
        else:
            return self.get_student_name(id)

    def get_admin_id(self):
        """returns the admin's id"""
        self.open_DB()
        sql = "SELECT Id \nFROM Teachers \nWHERE isAdmin='True';"
        print(sql)
        self.current.execute(sql)
        id = self.current.fetchone()[0]
        self.close_DB()
        return id

    def get_day_data(self, teacherId, date):
        """returns the data of the received day: startTime, studentId, pickUpLocation,student name"""
        self.open_DB()
        sql = "SELECT startTime, studentId, pickUpLocation \nFROM Lessons \nWHERE teacherId='%s' AND date='%s' \nORDER BY startTime;" % (teacherId, date)
        print(sql)
        self.current.execute(sql)
        times_dict = {}
        for row in self.current:
            startTime = row[0]
            times_dict[startTime] = "|" + row[0] + "#" + row[1] + "#" + row[2]  # the data: |startTime#studentId#pickUpLocation

        # to add the name of each student to the dictionary of the lessons
        for time in times_dict.keys():
            student_id = times_dict[time].split("#")[1]
            name = self.get_student_name(student_id)
            times_dict[time] += ("#" + name)
        self.close_DB()

        # fill the empty hours:
        start_shift = int(START_SHIFT.split(":")[0])
        end_shift = int(END_SHIFT.split(":")[0])

        for i in range(start_shift, end_shift):  # go over all the hours in the shift and check if not occupied:
            empty_time = str(i).zfill(2) + ":00"  # the needed time:for example - 08:00
            if empty_time not in times_dict.keys():
                times_dict[empty_time] = "|" + empty_time + "# # # "  # empty lesson

        times_dict = dict(sorted(times_dict.items()))  # sort again- after the new update of the empty lessons

        all_message = ""
        for key in times_dict.keys():
            all_message += times_dict[key]

        return all_message  # the data: |startTime#studentId#pickUpLocation#studentName

    def get_occupied_lessons(self, teacherId, date):
        """returns a list of all the occupied lessons of the teacher on the received day"""
        self.open_DB()
        sql = "SELECT startTime \nFROM Lessons \nWHERE teacherId='%s' AND date='%s' \nORDER BY startTime;" % (teacherId, date)
        print(sql)
        self.current.execute(sql)
        occupied_times = []
        for row in self.current:  # a list of all the occupied times:
            occupied_times.append(row[0])
        self.close_DB()

        return occupied_times

    def get_student_lessons_in_day(self, studentId, teacherId, date):
        """returns the student's lesson in a specific date (if he has)"""
        self.open_DB()
        sql = "SELECT startTime, pickUpLocation, price \nFROM Lessons \nWHERE studentId='%s' AND date='%s' AND teacherId='%s';" % (studentId, date, teacherId)
        print(sql)
        self.current.execute(sql)
        times_dict = {}
        for row in self.current:
            times_dict[row[0]] = "|" + row[0] + "#" + row[1] + "#" + str(row[2])

        return times_dict  # dictionary: key:time, value:time+location+price

    def get_student_day_data(self, studentId, teacherId, date):
        """returns the student's day data- his teacher's free lessons + the lesson that he set with him"""
        occupied_times = self.get_occupied_lessons(teacherId, date)
        student_lessons = self.get_student_lessons_in_day(studentId, teacherId, date)

        start_shift = int(START_SHIFT.split(":")[0])
        end_shift = int(END_SHIFT.split(":")[0])
        all_message = ""

        if len(student_lessons.keys()) != 0:  # the student has lesson in that day
            for i in range(start_shift, end_shift):  # go over all the hours in the shift and check if not occupied:
                time = str(i).zfill(2) + ":00"  # the needed time:for example - 08:00
                if time not in occupied_times:  # if it's free time:
                    all_message += "|" + time
                elif time in student_lessons.keys():  # the student has a lesson
                    all_message += student_lessons[time]

        else:
            for i in range(start_shift, end_shift):  # go over all the hours in the shift and check if not occupied:
                time = str(i).zfill(2) + ":00"  # the needed time:for example - 08:00
                if time not in occupied_times:  # if it's free time:
                    all_message += "|" + time

        return all_message

    def get_notifications(self, receiverId):
        """returns all the user's notifications' data : the sender, the content, status, sendingTime"""
        self.open_DB()
        sql = "SELECT senderId, content, status, sendingTime, type \nFROM Notifications \nWHERE receiverId='%s';" % receiverId
        print(sql)
        self.current.execute(sql)

        notifications_dict = {} # key=(senderId, receiverId, sendingTime), value=|senderId#content#status#sendingTime#type

        for row in self.current:
            notifications_dict[(row[0], receiverId, row[3])] = "|" + row[0] + "#" + row[1] + "#" + row[2] + "#" + row[3] + "#" + row[4]  # the data: |senderId#content#status#sendingTime#type

        # sorting the lessons by the sendingTime : the latest first. item[0] = (senderId, receiverId, sendingTime)
        sorted_dict = dict(sorted(notifications_dict.items(), key=lambda item: datetime.strptime(item[0][2], '%d.%m.%Y %H:%M'),reverse=True))

        all_message = ""
        # to add the sender name for each notification to the dictionary
        for key, value in sorted_dict.items():
            sender_id = value.split("#")[0].replace("|", "")  # to get only the sender id (without the char |)
            sender_name = self.get_user_name(sender_id)
            value += ("#" + sender_name)
            all_message += value

        self.close_DB()
        return all_message

    def get_notification_type_data(self, senderId, receiverId, sendingTime):
        """returns the type and data of the notification"""
        self.open_DB()
        sql = "SELECT type, data \nFROM Notifications \nWHERE senderId='%s' AND receiverId='%s' AND sendingTime='%s';" % (senderId, receiverId, sendingTime)
        print(sql)
        self.current.execute(sql)
        lst = self.current.fetchone()
        print("lst", lst)
        self.close_DB()
        return lst[0], lst[1]  # type, data

    def get_student_lessons(self, studentId):
        """returns the total lessons, total paid, total debt. And the data of the lessons of the specific student:
         for each lesson:date, startTime, duration, price, pickUpLocation,isPaid"""
        self.open_DB()
        sql = "SELECT date, startTime, duration, price, pickUpLocation, isPaid \nFROM Lessons \nWHERE studentId='%s';" % studentId
        print(sql)
        self.current.execute(sql)

        # creating a dictionary: key=(date, time), value=date#startTime#duration#price#pickUpLocation#isPaid
        date_time_dict = {}

        count = 0
        sum_paid = 0
        sum_debt = 0
        for row in self.current:
            date_time_dict[(row[0], row[1])] = "|" + row[0] + "#" + row[1] + "#" + str(row[2]) + "#" + str(row[3]) + "#" + row[4] + "#" + row[5]  # the data: |date#startTime#duration#price#pickUpLocation#isPaid
            if row[5] == "True":  # if isPaid == True
                sum_paid += row[3]  # sum_paid += price
            else:
                sum_debt += row[3]
            count += 1
        self.close_DB()

        # sorting the lessons by dates+time : the latest first. item[0] = (date, time)
        sorted_dict = dict(sorted(date_time_dict.items(), key=lambda item: datetime.strptime(item[0][0] + " " + item[0][1], '%d.%m.%Y %H:%M'),reverse=True))

        all_message = "|" + str(count) + "|" + str(sum_paid) + "|" + str(sum_debt)  # creating the message to send
        for v in sorted_dict.values():
            all_message += v

        return all_message

    def get_accepted_teachers(self):
        """returns a list of all the accepted teachers' id,name,area,phone,seniority of a specific teacher"""
        self.open_DB()
        sql = "SELECT Id, name, area, phoneNum, price, seniority \nFROM Teachers \nWHERE isAccepted='True' \nORDER BY name;"
        print(sql)
        self.current.execute(sql)
        teachers_dict = {}
        for row in self.current:
            teacher_id = row[0]
            teachers_dict[teacher_id] = "|" + row[0] + "#" + row[1] + "#" + row[2] + "#" + row[3] + "#" + str(row[4]) + "#" + row[5]  # the data: |id#name#area#phone#price#seniority
        self.close_DB()

        all_message = ""
        # to add the car for each teacher to the dictionary
        for id in teachers_dict.keys():
            car = self.get_teacher_car(id)
            teachers_dict[id] += ("#" + car[0] + " " + car[1] + " " + car[2] + "#" + car[3])  # the data: model year size#type
            all_message += teachers_dict[id]

        return all_message

    # __________________________________________________________________________________________________________________
    # __________________________________________________________________________________________________________________
    # ______end of read start write ____________________________________________________________________________________
    # __________________________________________________________________________________________________________________
    # __________________________________________________________________________________________________________________
    # __________________________________________________________________________________________________________________

    # All write SQL
    def insert_new_teacher(self, teacher):
        """insert new teacher to the table Teachers"""
        self.open_DB()
        sql = "INSERT INTO Teachers VALUES('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');" % \
              (teacher.email, teacher.Id, teacher.name, teacher.area, teacher.birthdate,
               teacher.phone_num, teacher.seniority, teacher.address, teacher.price, teacher.isAdmin, teacher.isAccepted,
               teacher.lastEntry, teacher.createdDate)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def insert_new_student(self, student):
        """insert new student to the table Students"""
        self.open_DB()
        sql = "INSERT INTO Students VALUES('%s','%s','%s','%s','%s','%s','%s','%s','%s');" % \
              (student.email, student.Id, student.teacherId, student.name, student.birthdate,
               student.address, student.phone_num, student.lastEntry, student.createdDate)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def insert_new_password(self, password):
        """insert new password to the table Passwords"""
        self.open_DB()
        sql = "INSERT INTO Passwords VALUES('%s','%s','%s');" % \
              (password.Id, password.hashedPass, password.salt)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def insert_new_car(self, car):
        """insert new car to the table Cars"""
        self.open_DB()
        sql = "INSERT INTO Cars VALUES('%s','%s','%s','%s','%s');" % \
              (car.teacherId, car.car_type, car.year, car.model, car.category_size)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def insert_new_lesson(self, lesson):
        """insert new lesson to the table Lessons"""
        self.open_DB()
        sql = "INSERT INTO Lessons VALUES('%s','%s','%s','%s','%s','%s','%s','%s');" % \
              (lesson.student_id, lesson.teacher_id, lesson.date, lesson.startTime, lesson.price, lesson.duration,
               lesson.pick_up_location, lesson.isPaid)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def insert_new_notification(self, notification):
        """insert new notification to the table Notifications"""
        self.open_DB()
        sql = "INSERT INTO Notifications VALUES('%s','%s','%s','%s','%s','%s','%s');" % \
              (notification.sender_id, notification.receiver_id, notification.type, notification.content,
               notification.data, notification.status, notification.sendingTime)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_teacher_info(self, id, area, seniority, price):
        """update the values area, seniority, price in Teachers table"""
        self.open_DB()
        sql = "UPDATE Teachers \nSET area='%s', seniority='%s', price='%s' \nWHERE Id='%s';" % (area, seniority, price, id)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_teacher_accepted(self, teacherId):
        """ updates the teacher to accepted"""
        self.open_DB()
        sql = "UPDATE Teachers \nSET isAccepted='True' \nWHERE Id='%s';" % teacherId
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_pass(self, Id, temp):
        """update the password to the received password"""
        self.open_DB()
        sql = "UPDATE Passwords \nSET hashedPass='%s' \nWHERE Id='%s';" % (temp, Id)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_lastEntry(self, Id, date, table):
        """update the last entry of the user in the tables: Teachers/Students"""
        self.open_DB()
        sql = "UPDATE %s \nSET lastEntry='%s' \nWHERE Id='%s';" % (table, date, Id)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_paid_status(self, date, time, studentId, curr_paid_status):
        """update the paid status of the lesson in the table Lessons"""
        if curr_paid_status == "True":  # the current lesson status
            is_paid = False
        else:
            is_paid = True

        self.open_DB()
        sql = "UPDATE Lessons \nSET isPaid='%s' \nWHERE date='%s' AND startTime='%s' AND studentId='%s';" % (
        is_paid, date, time, studentId)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_notification_status(self, senderId, receiverId, sendingTime, status):
        """updates the notification status- True/False according to the receiver input"""
        self.open_DB()
        sql = "UPDATE Notifications \nSET status='%s' \nWHERE senderId='%s' AND receiverId='%s' AND sendingTime='%s';" % (status, senderId, receiverId, sendingTime)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def update_student_teacher(self, studentId, teacherId):
        """updates the new student's teacher"""
        self.open_DB()
        sql = "UPDATE Students \nSET teacherId='%s' \nWHERE Id='%s';" % (teacherId, studentId)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_lesson(self, senderId, receiverId, date, startTime):
        """deletes the received lesson"""

        if self.check_value_already_exist("Teachers", "Id", senderId):  # checks if the sender is a teacher
            teacherId = senderId
            studentId = receiverId
        else:
            teacherId = receiverId
            studentId = senderId

        self.open_DB()
        sql = "DELETE FROM Lessons \nWHERE studentId ='%s' AND teacherId ='%s' AND date='%s' AND startTime='%s';" % (studentId, teacherId, date, startTime)
        print(sql)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_teacher(self, teacherId):
        """deletes the teacher by teacher's id"""
        self.open_DB()
        sql = "DELETE FROM Teachers \nWHERE Id ='%s';" % teacherId
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_student(self, studentId):
        """deletes the student by student's id"""
        self.open_DB()
        sql = "DELETE FROM Students \nWHERE Id ='%s';" % studentId
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_password(self, id):
        """deletes the password by user's id"""
        self.open_DB()
        sql = "DELETE FROM Passwords \nWHERE Id ='%s';" % id
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_notification(self, senderId):
        """deletes all notifications that was sent by the senderId"""
        self.open_DB()
        sql = "DELETE FROM Notifications \nWHERE senderId ='%s';" % senderId
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_notification(self, senderId, receiverId, sendingTime):
        """deletes a specific notification that was sent."""
        self.open_DB()
        sql = "DELETE FROM Notifications \nWHERE senderId ='%s' AND receiverId='%s' AND sendingTime='%s';" % (senderId, receiverId, sendingTime)
        self.current.execute(sql)
        self.commit()
        self.close_DB()

    def delete_join_requests(self, studentId, teacherId):
        """deletes all the other student's join requests (to other teachers)"""
        self.open_DB()
        sql = "DELETE FROM Notifications \nWHERE senderId ='%s' AND receiverId!='%s';" % (studentId, teacherId)
        self.current.execute(sql)
        self.commit()
        self.close_DB()


def main_test():
    print("test")
    db = DriveORM()
    #  print(db.check_email_id_already_exist("my@","1122"))


if __name__ == "__main__":
    main_test()
