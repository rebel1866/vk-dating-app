use vk_dating_app;

db.recognizes.insertMany([
    {
        _id:1,
        faceUid:"430eb1ad-fe36-11ed-bdb4-0cc47a6c4dbd",
        url:"https://www.film.ru/sites/default/files/people/1909572-1081854.jpg",
        nameSpace:"samara-weaving@mynamespace"
    },
    {
        _id:2,
        faceUid:"5389c2b5-06c6-11ee-bdb4-0cc47a6c4dbd",
        url:"https://znaydaydzest.ru/wp-content/uploads/2021/09/aktrisa-elle-fanning-26.jpg",
        nameSpace:"ell-fanning@mynamespace"
    },
]);

// mongosh < addRecognizeFaces.js