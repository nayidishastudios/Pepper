# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20130624062347) do

  create_table "comits", :force => true do |t|
    t.integer  "repository_id"
    t.string   "comit_hash"
    t.text     "message"
    t.boolean  "is_update"
    t.datetime "time"
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  create_table "fils", :force => true do |t|
    t.integer  "comit_id"
    t.text     "filename"
    t.integer  "size"
    t.integer  "action",     :null => false
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
  end

  create_table "groups", :force => true do |t|
    t.string   "name"
    t.text     "contact"
    t.text     "comments"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
  end

  create_table "installers", :force => true do |t|
    t.string   "name"
    t.string   "username"
    t.string   "password_digest"
    t.text     "contact"
    t.datetime "created_at",      :null => false
    t.datetime "updated_at",      :null => false
  end

  create_table "machines", :force => true do |t|
    t.string   "machine_id"
    t.string   "name"
    t.integer  "school_id"
    t.text     "ssl_key"
    t.datetime "created_at", :null => true
    t.datetime "updated_at", :null => true
  end

  create_table "purchases", :force => true do |t|
    t.integer  "typ"
    t.integer  "typ_id"
    t.integer  "repository_id"
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  create_table "repositories", :force => true do |t|
    t.string   "name"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
  end

  create_table "schools", :force => true do |t|
    t.integer  "group_id"
    t.string   "name"
    t.text     "contact"
    t.text     "comments"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
  end

  create_table "update_records", :force => true do |t|
    t.integer  "update_id"
    t.integer  "machine_id"
    t.datetime "created_at", :null => true
    t.datetime "updated_at", :null => true
  end

  create_table "updates", :force => true do |t|
    t.integer  "level"
    t.integer  "typ"
    t.integer  "typ_id"
    t.integer  "repository_id"
    t.integer  "comit_id"
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  create_table "users", :force => true do |t|
    t.string   "name"
    t.string   "email"
    t.string   "remember_token"
    t.string   "password_digest"
    t.datetime "created_at",      :null => false
    t.datetime "updated_at",      :null => false
  end

end
