# locustfile.py
# locust 테스트에 필요한 패키지 설치
# python 설치: brew install python
# locust 설치: brew install locust

# 아래의 명령으로 테스트
# locust -f ./locustfile.py --host=https://localhost:8080
from locust import HttpUser, task, between, TaskSet
import random


# class UserBehavior(TaskSet):
#     @task
#     def get_user_detail(self):
#         user_id = 1
#         self.client.get(f'/front/recent-search')
#
#     @task
#     def home(self):
#         self.client.get('/')
class GetNestedBehavior(TaskSet):
    @task
    def inner_task(self):
        self.client.get('/recent-search')

    def on_stop(self):
        self.interrupt()


class DeleteNestedBehavior(TaskSet):
    @task
    def inner_task(self):
        self.client.delete('/recent-search')

    def on_stop(self):
        self.interrupt()


class Sub1(TaskSet):
    @task
    def inner_task(self):
        number = random.randrange(1, 1000)
        numberStr = str(number).zfill(4)
        print("Sub1: " + numberStr)

    def on_stop(self):
        print("Sub1 stop")
        self.interrupt()


class Sub2(TaskSet):
    @task
    def inner_task(self):
        number = random.randrange(1, 1000)
        numberStr = str(number).zfill(4)
        print("Sub2: " + numberStr)

    def on_stop(self):
        print("Sub2 stop")
        self.interrupt()


class MainBehavior(TaskSet):
    # tasks = [GetNestedBehavior]
    # tasks = [DeleteNestedBehavior]
    # tasks = {Sub1, Sub2}
    def on_start(self):

        #self.client.headers = {
        #    "Content-Type": "application/json",
        #    "Authorization": "Bearer TOKEN"
        #}

    @task
    def main_task(self):
        # for number in range(1000):
        number = random.randrange(1, 100)
        numberStr = str(number).zfill(3)
        r = self.client.post('/recent-search', json={
            "member_no": "20230822101306776" + numberStr,
            "idvisitor": "eb30dfe378b38c58" + numberStr,
            "recent_search": {
                "nationcode": "KR",
                "type": 3,
                "division": "hotel",
                "keyword": "파르나스 호텔 제주, 서귀포시, 한국",
                "keyword_id": "100" + numberStr,
                "guestInfos": "2~2,3~10,4~11",
                "from": "2024-12-28",
                "to": "2024-12-31"
            }
        })
        print("POST {}: {}, message: {}".format(numberStr, r.status_code, r))

    @task
    def get_task(self):
        number = random.randrange(1, 100)
        numberStr = str(number).zfill(3)
        r = self.client.get(f"/recent-search?member_no=20230822101306776{numberStr}&idvisitor=eb30dfe378b38c58{numberStr}")
        print("GET {}: {}, message: {}".format(numberStr, r.status_code, r))

    @task
    def delete_task(self):
        number = random.randrange(1, 100)
        numberStr = str(number).zfill(3)
        r = self.client.delete(f"/recent-search?member_no=20230822101306776{numberStr}&idvisitor=eb30dfe378b38c58{numberStr}&type=3&keyword_id=100{numberStr}")
        print("DELETE {}: {}, message: {}".format(numberStr, r.status_code, r))


class LocustUser(HttpUser):
    host = "https://localhost:8080"
    tasks = [MainBehavior]

    wait_time = between(1, 1)
