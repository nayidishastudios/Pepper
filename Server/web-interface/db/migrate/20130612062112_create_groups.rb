class CreateGroups < ActiveRecord::Migration
  def change
    create_table :groups do |t|
      t.string :name
      t.text :contact
      t.text :comments

      t.timestamps
    end
  end
end
