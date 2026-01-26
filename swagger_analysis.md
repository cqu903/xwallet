uid=4_0 RootWebArea "Swagger UI" url="http://localhost:8080/api/swagger-ui/index.html#/"
  uid=4_1 ignored
    uid=4_2 ignored
      uid=4_3 generic
        uid=4_4 generic
          uid=4_5 ignored
            uid=4_6 ignored
              uid=4_7 ignored
                uid=4_8 ignored
                  uid=4_9 image
                uid=4_10 generic
                  uid=4_11 textbox value="/api/v3/api-docs"
                    uid=4_12 generic
                      uid=4_13 StaticText "/api/v3/api-docs"
                  uid=4_14 button "Explore"
                    uid=4_15 StaticText "Explore"
          uid=4_16 ignored
            uid=4_17 generic
              uid=4_18 image
            uid=4_19 ignored
              uid=4_20 ignored
                uid=4_21 generic
                  uid=4_22 ignored
                    uid=4_23 ignored
                      uid=4_24 group
                        uid=4_25 heading "XWallet 后端 API 1.0.0 OAS 3.0" level="2"
                          uid=4_26 StaticText "XWallet 后端 API"
                            uid=4_27 InlineTextBox "XWallet 后端 API"
                          uid=4_28 generic
                            uid=4_29 generic
                              uid=4_30 StaticText " "
                              uid=4_31 StaticText "1.0.0"
                              uid=4_32 StaticText " "
                          uid=4_33 generic
                            uid=4_34 generic
                              uid=4_35 StaticText "OAS "
                              uid=4_36 StaticText "3.0"
                        uid=4_37 link "/api/v3/api-docs" url="http://localhost:8080/api/v3/api-docs"
                          uid=4_38 StaticText "/api/v3/api-docs"
                      uid=4_39 ignored
                        uid=4_40 ignored
                          uid=4_41 paragraph
                            uid=4_42 StaticText "xWallet 钱包后端服务 REST 接口。"
                          uid=4_43 heading "认证说明" level="2"
                            uid=4_44 StaticText "认证说明"
                          uid=4_45 list
                            uid=4_46 listitem level="1"
                              uid=4_47 ListMarker "• "
                                uid=4_48 ignored
                              uid=4_49 strong
                                uid=4_50 StaticText "系统用户（管理后台）"
                              uid=4_51 StaticText "："
                              uid=4_52 code
                                uid=4_53 StaticText "userType=SYSTEM"
                              uid=4_54 StaticText "，"
                              uid=4_55 code
                                uid=4_56 StaticText "account"
                              uid=4_57 StaticText " 为工号，需配合 JWT 访问需权限接口。"
                            uid=4_58 listitem level="1"
                              uid=4_59 ListMarker "• "
                                uid=4_60 ignored
                              uid=4_61 strong
                                uid=4_62 StaticText "顾客（移动端）"
                              uid=4_63 StaticText "："
                              uid=4_64 code
                                uid=4_65 StaticText "userType=CUSTOMER"
                              uid=4_66 StaticText "，"
                              uid=4_67 code
                                uid=4_68 StaticText "account"
                              uid=4_69 StaticText " 为邮箱。"
                          uid=4_70 heading "通用响应格式" level="2"
                            uid=4_71 StaticText "通用响应格式"
                          uid=4_72 paragraph
                            uid=4_73 code
                              uid=4_74 StaticText "{ "code": 200, "message": "success", "data": ... }"
                            uid=4_75 StaticText "，失败时 "
                            uid=4_76 code
                              uid=4_77 StaticText "code"
                            uid=4_78 StaticText " 非 200，"
                            uid=4_79 code
                              uid=4_80 StaticText "data"
                            uid=4_81 StaticText " 可为空。"
                      uid=4_82 ignored
                        uid=4_83 generic
                          uid=4_84 link "XWallet - Website" url="https://github.com/zerofinance/xwallet"
                            uid=4_85 StaticText "XWallet"
                            uid=4_86 StaticText " - Website"
                      uid=4_87 generic
                        uid=4_88 StaticText "Proprietary"
              uid=4_89 ignored
                uid=4_90 generic
                  uid=4_91 ignored
                    uid=4_92 ignored
                      uid=4_93 StaticText "Servers"
                      uid=4_94 generic
                        uid=4_95 LabelText
                          uid=4_96 combobox expandable haspopup="menu" value="/api - 本地 / 默认（与 server.servlet.context-path 一致）"
                            uid=4_97 MenuListPopup
                              uid=4_98 ignored
                                uid=4_99 option "/api - 本地 / 默认（与 server.servlet.context-path 一致）" selectable selected value="/api - 本地 / 默认（与 server.servlet.context-path 一致）"
                  uid=4_100 ignored
                    uid=4_101 button "Authorize"
                      uid=4_102 ignored
                        uid=4_103 StaticText "Authorize"
              uid=4_104 ignored
              uid=4_105 ignored
                uid=4_106 generic
                  uid=4_107 generic
                    uid=4_108 ignored
                      uid=4_109 heading "菜单 获取当前登录用户可见的菜单树（id、name、path、children），需 JWT 认证 Collapse operation" level="3"
                        uid=4_110 link "菜单" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%8F%9C%E5%8D%95"
                          uid=4_111 StaticText "菜单"
                        uid=4_112 ignored
                          uid=4_113 ignored
                            uid=4_114 paragraph
                              uid=4_115 StaticText "获取当前登录用户可见的菜单树（id、name、path、children），需 JWT 认证"
                        uid=4_116 button "Collapse operation" expandable expanded
                      uid=4_117 ignored
                        uid=4_118 generic
                          uid=4_119 generic
                            uid=4_120 ignored
                              uid=4_121 button "GET /menus 获取当前用户菜单列表" expandable
                                uid=4_122 ignored
                                  uid=4_123 StaticText "GET"
                                uid=4_124 ignored
                                  uid=4_125 ignored
                                    uid=4_126 link "/menus" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%8F%9C%E5%8D%95/getUserMenus"
                                      uid=4_127 StaticText "/menus"
                                  uid=4_128 generic
                                    uid=4_129 StaticText "获取当前用户菜单列表"
                              uid=4_130 generic description="Copy to clipboard"
                              uid=4_131 button "authorization button unlocked"
                              uid=4_132 button "get ​/menus" expandable
                    uid=4_133 ignored
                      uid=4_134 heading "测试 开发辅助接口，如生成 BCrypt 密码；无需认证 Collapse operation" level="3"
                        uid=4_135 link "测试" url="http://localhost:8080/api/swagger-ui/index.html#/%E6%B5%8B%E8%AF%95"
                          uid=4_136 StaticText "测试"
                        uid=4_137 ignored
                          uid=4_138 ignored
                            uid=4_139 paragraph
                              uid=4_140 StaticText "开发辅助接口，如生成 BCrypt 密码；无需认证"
                        uid=4_141 button "Collapse operation" expandable expanded
                      uid=4_142 ignored
                        uid=4_143 generic
                          uid=4_144 generic
                            uid=4_145 ignored
                              uid=4_146 button "GET /test /password 生成测试密码 BCrypt 哈希" expandable
                                uid=4_147 ignored
                                  uid=4_148 StaticText "GET"
                                uid=4_149 ignored
                                  uid=4_150 ignored
                                    uid=4_151 link "/test /password" url="http://localhost:8080/api/swagger-ui/index.html#/%E6%B5%8B%E8%AF%95/generatePasswords"
                                      uid=4_152 StaticText "/test"
                                      uid=4_153 ignored
                                      uid=4_154 StaticText "/password"
                                  uid=4_155 generic
                                    uid=4_156 StaticText "生成测试密码 BCrypt 哈希"
                              uid=4_157 generic description="Copy to clipboard"
                              uid=4_158 button "get ​/test​/password" expandable
                    uid=4_159 ignored
                      uid=4_160 heading "角色管理 角色的增删改查、启用/禁用；需 JWT 及 system:role 权限 Collapse operation" level="3"
                        uid=4_161 link "角色管理" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86"
                          uid=4_162 StaticText "角色管理"
                        uid=4_163 ignored
                          uid=4_164 ignored
                            uid=4_165 paragraph
                              uid=4_166 StaticText "角色的增删改查、启用/禁用；需 JWT 及 system:role 权限"
                        uid=4_167 button "Collapse operation" expandable expanded
                      uid=4_168 ignored
                        uid=4_169 generic
                          uid=4_170 generic
                            uid=4_171 ignored
                              uid=4_172 button "DELETE /role /{id} 删除角色" expandable
                                uid=4_173 ignored
                                  uid=4_174 StaticText "DELETE"
                                uid=4_175 ignored
                                  uid=4_176 ignored
                                    uid=4_177 link "/role /{id}" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/deleteRole"
                                      uid=4_178 StaticText "/role"
                                      uid=4_179 ignored
                                      uid=4_180 StaticText "/{id}"
                                  uid=4_181 generic
                                    uid=4_182 StaticText "删除角色"
                              uid=4_183 generic description="Copy to clipboard"
                              uid=4_184 button "authorization button unlocked"
                              uid=4_185 button "delete ​/role​/{id}" expandable
                          uid=4_186 generic
                            uid=4_187 ignored
                              uid=4_188 button "GET /role /{id} 根据 ID 获取角色详情" expandable
                                uid=4_189 ignored
                                  uid=4_190 StaticText "GET"
                                uid=4_191 ignored
                                  uid=4_192 ignored
                                    uid=4_193 link "/role /{id}" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/getRoleById"
                                      uid=4_194 StaticText "/role"
                                      uid=4_195 ignored
                                      uid=4_196 StaticText "/{id}"
                                  uid=4_197 generic
                                    uid=4_198 StaticText "根据 ID 获取角色详情"
                              uid=4_199 generic description="Copy to clipboard"
                              uid=4_200 button "authorization button unlocked"
                              uid=4_201 button "get ​/role​/{id}" expandable
                          uid=4_202 generic
                            uid=4_203 ignored
                              uid=4_204 button "GET /role /list 获取角色列表" expandable
                                uid=4_205 ignored
                                  uid=4_206 StaticText "GET"
                                uid=4_207 ignored
                                  uid=4_208 ignored
                                    uid=4_209 link "/role /list" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/getRoleList"
                                      uid=4_210 StaticText "/role"
                                      uid=4_211 ignored
                                      uid=4_212 StaticText "/list"
                                  uid=4_213 generic
                                    uid=4_214 StaticText "获取角色列表"
                              uid=4_215 generic description="Copy to clipboard"
                              uid=4_216 button "authorization button unlocked"
                              uid=4_217 button "get ​/role​/list" expandable
                          uid=4_218 generic
                            uid=4_219 ignored
                              uid=4_220 button "POST /role 创建角色" expandable
                                uid=4_221 ignored
                                  uid=4_222 StaticText "POST"
                                uid=4_223 ignored
                                  uid=4_224 ignored
                                    uid=4_225 link "/role" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/createRole"
                                      uid=4_226 StaticText "/role"
                                  uid=4_227 generic
                                    uid=4_228 StaticText "创建角色"
                              uid=4_229 generic description="Copy to clipboard"
                              uid=4_230 button "authorization button unlocked"
                              uid=4_231 button "post ​/role" expandable
                          uid=4_232 generic
                            uid=4_233 ignored
                              uid=4_234 button "PUT /role /{id} 更新角色" expandable
                                uid=4_235 ignored
                                  uid=4_236 StaticText "PUT"
                                uid=4_237 ignored
                                  uid=4_238 ignored
                                    uid=4_239 link "/role /{id}" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/updateRole"
                                      uid=4_240 StaticText "/role"
                                      uid=4_241 ignored
                                      uid=4_242 StaticText "/{id}"
                                  uid=4_243 generic
                                    uid=4_244 StaticText "更新角色"
                              uid=4_245 generic description="Copy to clipboard"
                              uid=4_246 button "authorization button unlocked"
                              uid=4_247 button "put ​/role​/{id}" expandable
                          uid=4_248 generic
                            uid=4_249 ignored
                              uid=4_250 button "PUT /role /{id} /status 启用/禁用角色" expandable
                                uid=4_251 ignored
                                  uid=4_252 StaticText "PUT"
                                uid=4_253 ignored
                                  uid=4_254 ignored
                                    uid=4_255 link "/role /{id} /status" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%A7%92%E8%89%B2%E7%AE%A1%E7%90%86/toggleRoleStatus"
                                      uid=4_256 StaticText "/role"
                                      uid=4_257 ignored
                                      uid=4_258 StaticText "/{id}"
                                      uid=4_259 ignored
                                      uid=4_260 StaticText "/status"
                                  uid=4_261 generic
                                    uid=4_262 StaticText "启用/禁用角色"
                              uid=4_263 generic description="Copy to clipboard"
                              uid=4_264 button "authorization button unlocked"
                              uid=4_265 button "put ​/role​/{id}​/status" expandable
                    uid=4_266 ignored
                      uid=4_267 heading "权限 获取当前用户权限、角色与菜单树；需 JWT 认证 Collapse operation" level="3"
                        uid=4_268 link "权限" url="http://localhost:8080/api/swagger-ui/index.html#/%E6%9D%83%E9%99%90"
                          uid=4_269 StaticText "权限"
                        uid=4_270 ignored
                          uid=4_271 ignored
                            uid=4_272 paragraph
                              uid=4_273 StaticText "获取当前用户权限、角色与菜单树；需 JWT 认证"
                        uid=4_274 button "Collapse operation" expandable expanded
                      uid=4_275 ignored
                        uid=4_276 generic
                          uid=4_277 generic
                            uid=4_278 ignored
                              uid=4_279 button "GET /permissions /mine 获取当前用户权限与菜单" expandable
                                uid=4_280 ignored
                                  uid=4_281 StaticText "GET"
                                uid=4_282 ignored
                                  uid=4_283 ignored
                                    uid=4_284 link "/permissions /mine" url="http://localhost:8080/api/swagger-ui/index.html#/%E6%9D%83%E9%99%90/getMyPermissions"
                                      uid=4_285 StaticText "/permissions"
                                      uid=4_286 ignored
                                      uid=4_287 StaticText "/mine"
                                  uid=4_288 generic
                                    uid=4_289 StaticText "获取当前用户权限与菜单"
                              uid=4_290 generic description="Copy to clipboard"
                              uid=4_291 button "authorization button unlocked"
                              uid=4_292 button "get ​/permissions​/mine" expandable
                    uid=4_293 ignored
                      uid=4_294 heading "认证 登录、登出、注册、验证码、Token 校验；登录/注册/发送验证码无需 Token Collapse operation" level="3"
                        uid=4_295 link "认证" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81"
                          uid=4_296 StaticText "认证"
                        uid=4_297 ignored
                          uid=4_298 ignored
                            uid=4_299 paragraph
                              uid=4_300 StaticText "登录、登出、注册、验证码、Token 校验；登录/注册/发送验证码无需 Token"
                        uid=4_301 button "Collapse operation" expandable expanded
                      uid=4_302 ignored
                        uid=4_303 generic
                          uid=4_304 generic
                            uid=4_305 ignored
                              uid=4_306 button "GET /auth /validate 验证 Token" expandable
                                uid=4_307 ignored
                                  uid=4_308 StaticText "GET"
                                uid=4_309 ignored
                                  uid=4_310 ignored
                                    uid=4_311 link "/auth /validate" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81/validateToken"
                                      uid=4_312 StaticText "/auth"
                                      uid=4_313 ignored
                                      uid=4_314 StaticText "/validate"
                                  uid=4_315 generic
                                    uid=4_316 StaticText "验证 Token"
                              uid=4_317 generic description="Copy to clipboard"
                              uid=4_318 button "authorization button unlocked"
                              uid=4_319 button "get ​/auth​/validate" expandable
                          uid=4_320 generic
                            uid=4_321 ignored
                              uid=4_322 button "POST /auth /send-code 发送验证码" expandable
                                uid=4_323 ignored
                                  uid=4_324 StaticText "POST"
                                uid=4_325 ignored
                                  uid=4_326 ignored
                                    uid=4_327 link "/auth /send-code" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81/sendVerificationCode"
                                      uid=4_328 StaticText "/auth"
                                      uid=4_329 ignored
                                      uid=4_330 StaticText "/send-code"
                                  uid=4_331 generic
                                    uid=4_332 StaticText "发送验证码"
                              uid=4_333 generic description="Copy to clipboard"
                              uid=4_334 button "post ​/auth​/send-code" expandable
                          uid=4_335 generic
                            uid=4_336 ignored
                              uid=4_337 button "POST /auth /register 用户注册" expandable
                                uid=4_338 ignored
                                  uid=4_339 StaticText "POST"
                                uid=4_340 ignored
                                  uid=4_341 ignored
                                    uid=4_342 link "/auth /register" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81/register"
                                      uid=4_343 StaticText "/auth"
                                      uid=4_344 ignored
                                      uid=4_345 StaticText "/register"
                                  uid=4_346 generic
                                    uid=4_347 StaticText "用户注册"
                              uid=4_348 generic description="Copy to clipboard"
                              uid=4_349 button "post ​/auth​/register" expandable
                          uid=4_350 generic
                            uid=4_351 ignored
                              uid=4_352 button "POST /auth /logout 用户登出" expandable
                                uid=4_353 ignored
                                  uid=4_354 StaticText "POST"
                                uid=4_355 ignored
                                  uid=4_356 ignored
                                    uid=4_357 link "/auth /logout" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81/logout"
                                      uid=4_358 StaticText "/auth"
                                      uid=4_359 ignored
                                      uid=4_360 StaticText "/logout"
                                  uid=4_361 generic
                                    uid=4_362 StaticText "用户登出"
                              uid=4_363 generic description="Copy to clipboard"
                              uid=4_364 button "authorization button unlocked"
                              uid=4_365 button "post ​/auth​/logout" expandable
                          uid=4_366 generic
                            uid=4_367 ignored
                              uid=4_368 button "POST /auth /login 用户登录" expandable
                                uid=4_369 ignored
                                  uid=4_370 StaticText "POST"
                                uid=4_371 ignored
                                  uid=4_372 ignored
                                    uid=4_373 link "/auth /login" url="http://localhost:8080/api/swagger-ui/index.html#/%E8%AE%A4%E8%AF%81/login"
                                      uid=4_374 StaticText "/auth"
                                      uid=4_375 ignored
                                      uid=4_376 StaticText "/login"
                                  uid=4_377 generic
                                    uid=4_378 StaticText "用户登录"
                              uid=4_379 generic description="Copy to clipboard"
                              uid=4_380 button "post ​/auth​/login" expandable
                    uid=4_381 ignored
                      uid=4_382 heading "用户管理 系统用户的增删改查、启用/禁用、重置密码；需权限：user:view / user:create / user:update / user:toggleStatus / user:resetPwd Expand operation" level="3"
                        uid=4_383 link "用户管理" url="http://localhost:8080/api/swagger-ui/index.html#/%E7%94%A8%E6%88%B7%E7%AE%A1%E7%90%86"
                          uid=4_384 StaticText "用户管理"
                        uid=4_385 ignored
                          uid=4_386 ignored
                            uid=4_387 paragraph
                              uid=4_388 StaticText "系统用户的增删改查、启用/禁用、重置密码；需权限：user:view / user:create / user:update / user:toggleStatus / user:resetPwd"
                        uid=4_389 button "Expand operation" expandable
              uid=4_390 ignored
                uid=4_391 generic
                  uid=4_392 generic
                    uid=4_393 heading "Schemas" level="4"
                      uid=4_394 button "Schemas" expandable focusable focused
                        uid=4_395 ignored
                          uid=4_396 StaticText "Schemas"
          uid=4_397 ignored
            uid=4_398 generic
