class Fil < ActiveRecord::Base
  attr_accessible :comit_id, :filename, :size, :action
  belongs_to :comit

  validates :filename, :presence => true
  validates :size, numericality: { only_integer: true }, :presence => true
  validates :action, numericality: { only_integer: true }, :presence => true, :inclusion => { :in => [ 0, 1, 2 ] }
  validate :comit_validity

  def comit_validity
    if !comit_id || !Comit.find(comit_id)
      errors.add(:comit_id, "Please choose a valid commit for the file.")
    end
  end
end
