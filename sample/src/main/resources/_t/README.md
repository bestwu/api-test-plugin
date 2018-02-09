#### 系统概述

系统接口采用REST规范，所有的接口以资源方式提供客户端访问。

[接口签名算法](签名算法.md)

RESTful Web 服务：

请求方法 | URI | 说明 | 响应的HTTP状态码
---|---|---|---
GET | /{model} | 资源列表 | 200
GET | /{model}/{id} | id对应的资源 | 200
POST | /{model} | 新建资源 | 201
PUT | /{model}/{id} | 更新资源 | 200
DELETE | /{model}/{id} | 删除资源 | 204
DELETE | /{model} | 批量删除资源 | 204

由于restful的特点，服务端不保存用户的状态，所以每次用户访问都须传输自身信息。

#### 客户端请求说明

客户端与服务器的通讯通过HTTP内容协商。所以，HTTP请求头应遵守以下规范：

* Accept的格式为:application/json;version=version

> version参数表示要请求的接口的版本。如果不传,服务器将按最低版本响应，强烈建议每次都传入确定的版本信息。

> 服务器接受的有效的Accept格式,(\*/* 、 application/* 、 application/json), 例子： \*/\*;version=1.0、application/*;version=1.0、application/json;version=1.0
错误的Accept服务器将响应406 HTTP Status Code

* User-Agent的格式建议统一为: npk/version (deviceInfo)

> npk 为项目代号，固定不变

> version 为客户端版本号

> deviceInfo 为平台信息

> 有效User-Agent,如：npk/1.0 (iPhone; iOS 8.1.2; Scale/2.00)、npk/1.0 (Android-4.4.4;Model:MI 4LTE)

#### 响应说明

响应结果状态通过HTTP状态码表达。返回的时间，默认格式为毫秒数(从1970年1月1日（UTC/GMT的午夜）开始所经过的毫秒数)

##### 响应内容

请求出错时的响应结果，即HTTP状态码>=400时,例子：

``` json
{
  "status": 422,
  "message": "Validation Failed",
  "errors": {
    "first_name": "First name cannot have fancy characters",
    "password": "Password cannot be blank"
  }
}
```

* status：错误码。当不是业务相关错误时，值与HTTP状态码保持一致，否则为[业务相关错误码](业务相关错误码.md)
* message：错误信息
* errors：详细错误信息，errors 内key为错误的字段/错误信息名称，value为错误信息

请求成功时的响应结果，HTTP状态码>=200,<300时：

###### 单个资源

例子：

```json
{
  "create_time": 1442309074,
  "id": "1",
  "modify_time": 1442309074,
  "username": "peter"
}
```
###### 列表资源

请求参数支持：page、size、sort

* page: 页码索引，从1开始，默认1
* size: 每页数量，默认20

例子：

```json
{
      "size": 13,
      "total": 13,
      "pages": 1,
      "list": {
        "id": 3,
        "goodsNo": "718000872243",
        "consigneeName": "\u8d75\u56fd\u67f1",
        "consigneeTel": "18224060100",
        "consigneeAddress": "\u4e3d\u666f\u56ed3\u671f",
        "status": 2,
        "finished": false
      },
      "page": 1
}
```

* list: 资源列表
* page: 页码，从1开始
* size: 每页数量
* total: 总数量
* pages: 总页数
 
##### 出错处理

当客户端收到的 HTTP 状态码大于等于400时，客户端应按以下方式处理。

* 当 HTTP 状态码为400时,表示业务异常，根据响应体中的 status 字段值按[业务相关错误码约定](业务相关错误码.md)处理。

* 当 HTTP 状态码为401时,跳转到登录界面，让用户登录,可提示用户您还未登录。

* 当 HTTP 状态码为其他时，取 message 字段内容提示用户即可。

##### HTTP状态码说明

HTTP状态码 | 说明
--- | ---
200 | 请求成功
201 | 资源成功创建
204 | 请求成功，返回空内容
400 | 错误的请求
401 | 未授权
403 | 无权访问
404 | 未找到资源
405 | 不支持的请求方法
406 | 不接受的客户端请求
422 | 请求数据验证失败，参数不正确
500 | 服务器通用错误码